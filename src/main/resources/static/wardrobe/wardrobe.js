/**
 * 衣橱主页脚本：用户信息、分类切换、素材上传/列出/删除、套装生成与任务轮询。
 * 请求统一走 /common/http.js 的 Http 封装。
 */
(function () {
    // 任务状态（与后端 TaskStatus 常量一致）
    var TASK_STATUS_SUCCEEDED = 'SUCCEEDED';
    var TASK_STATUS_FAILED = 'FAILED';
    // 任务轮询间隔（毫秒）
    var POLL_INTERVAL_MS = 2000;
    // 分类顺序与后端 AssetCategory 一致
    var CATEGORIES = [
        {code: 'AVATAR', label: '人物'},
        {code: 'HAT', label: '帽子'},
        {code: 'TOP', label: '上衣'},
        {code: 'PANTS', label: '裤子'},
        {code: 'SOCKS', label: '袜子'},
        {code: 'SHOES', label: '鞋子'}
    ];
    var MAX_PER_CATEGORY = 10;
    var currentCategory = 'AVATAR';
    var currentAssets = [];

    var tabsEl = document.getElementById('categoryTabs');

    // 加载当前用户，未登录（响应非 JSON 或 success=false）回登录页
    Http.get('/api/auth/me').then(function (result) {
        if (!result || !result.success) {
            location.href = '/auth/login.html';
            return;
        }
        document.getElementById('currentUser').textContent =
            '你好，' + (result.data.nickname || result.data.username);
    });

    document.getElementById('logoutBtn').addEventListener('click', async function () {
        await Http.post('/api/auth/logout', {});
        location.href = '/auth/login.html';
    });

    // 渲染分类标签
    CATEGORIES.forEach(function (category) {
        var btn = document.createElement('button');
        btn.textContent = category.label;
        btn.dataset.code = category.code;
        btn.addEventListener('click', function () {
            currentCategory = category.code;
            refreshTabs();
            loadAssets();
        });
        tabsEl.appendChild(btn);
    });

    function refreshTabs() {
        tabsEl.querySelectorAll('button').forEach(function (btn) {
            btn.className = btn.dataset.code === currentCategory ? 'active' : '';
        });
    }

    // 列出当前分类图片
    async function loadAssets() {
        var messageEl = document.getElementById('assetMessage');
        messageEl.textContent = '';
        var result = await Http.get('/api/assets?category=' + currentCategory);
        if (!result.success) {
            messageEl.textContent = result.errMsg;
            return;
        }
        currentAssets = result.data || [];
        document.getElementById('countHint').textContent =
            currentAssets.length + ' / ' + MAX_PER_CATEGORY + ' 张';
        renderGrid();
    }

    function renderGrid() {
        var gridEl = document.getElementById('assetGrid');
        gridEl.innerHTML = '';
        currentAssets.forEach(function (asset) {
            var wrap = document.createElement('div');
            wrap.className = 'item';
            var img = document.createElement('img');
            img.src = asset.url;
            img.alt = asset.fileName;
            var removeBtn = document.createElement('button');
            removeBtn.className = 'remove';
            removeBtn.textContent = '×';
            removeBtn.addEventListener('click', function () {
                deleteAsset(asset.assetId);
            });
            wrap.appendChild(img);
            wrap.appendChild(removeBtn);
            gridEl.appendChild(wrap);
        });
    }

    // 上传（支持多选，单次最多 10 张）
    document.getElementById('uploadBtn').addEventListener('click', async function () {
        var fileInput = document.getElementById('fileInput');
        var messageEl = document.getElementById('assetMessage');
        if (!fileInput.files.length) {
            messageEl.textContent = '请先选择图片';
            return;
        }
        if (fileInput.files.length > MAX_PER_CATEGORY) {
            messageEl.textContent = '单次最多上传 ' + MAX_PER_CATEGORY + ' 张图片';
            return;
        }
        var formData = new FormData();
        for (var i = 0; i < fileInput.files.length; i++) {
            formData.append('files', fileInput.files[i]);
        }
        messageEl.textContent = '上传中...';
        var result = await Http.postForm('/api/assets/upload-batch?category=' + currentCategory, formData);
        if (result.success) {
            messageEl.textContent = '上传成功，共 ' + result.data.length + ' 张';
            fileInput.value = '';
            loadAssets();
        } else {
            messageEl.textContent = result.errMsg;
        }
    });

    // 修改密码：成功后跳回登录页重新登录
    document.getElementById('changePasswordBtn').addEventListener('click', async function () {
        var oldPassword = document.getElementById('oldPassword').value;
        var newPassword = document.getElementById('newPassword').value;
        var messageEl = document.getElementById('passwordMessage');
        if (!oldPassword || !newPassword) {
            messageEl.textContent = '请填写原密码和新密码';
            return;
        }
        var result = await Http.post('/api/auth/change-password', {
            oldPassword: oldPassword,
            newPassword: newPassword
        });
        if (result.success) {
            alert('密码修改成功，请重新登录');
            location.href = '/auth/login.html';
        } else {
            messageEl.textContent = result.errMsg;
        }
    });

    // 删除
    async function deleteAsset(assetId) {
        var result = await Http.del('/api/assets?assetId=' + encodeURIComponent(assetId));
        if (result.success) {
            loadAssets();
        } else {
            document.getElementById('assetMessage').textContent = result.errMsg;
        }
    }

    // 生成套装：提交后轮询任务结果
    document.getElementById('generateBtn').addEventListener('click', async function () {
        var messageEl = document.getElementById('generateMessage');
        var resultBox = document.getElementById('resultBox');
        var prompt = document.getElementById('prompt').value.trim();
        messageEl.textContent = '生成中，链式试穿可能需要一些时间...';
        resultBox.innerHTML = '';
        var result = await Http.post('/api/outfit/generate', {prompt: prompt});
        if (!result.success) {
            messageEl.textContent = result.errMsg;
            return;
        }
        pollTask(result.data.taskId);
    });

    async function pollTask(taskId) {
        var messageEl = document.getElementById('generateMessage');
        var result = await Http.get('/api/outfit/tasks/' + taskId);
        if (!result.success) {
            messageEl.textContent = result.errMsg;
            return;
        }
        var task = result.data;
        if (task.status === TASK_STATUS_SUCCEEDED && task.imageUrl) {
            messageEl.textContent = '生成成功';
            var img = document.createElement('img');
            img.src = task.imageUrl;
            document.getElementById('resultBox').appendChild(img);
            return;
        }
        if (task.status === TASK_STATUS_FAILED) {
            messageEl.textContent = '生成失败: ' + (task.message || '未知错误');
            return;
        }
        setTimeout(function () { pollTask(taskId); }, POLL_INTERVAL_MS);
    }

    refreshTabs();
    loadAssets();
})();
