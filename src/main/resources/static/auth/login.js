/**
 * 登录页脚本：已登录自动跳转衣橱；提交账号密码调用登录接口。
 */
(function () {
    // 已登录则直接进入衣橱主页（未登录时被拦截器重定向，Http 封装返回 null）
    Http.get('/api/auth/me').then(function (result) {
        if (result && result.success) {
            location.href = '/wardrobe/wardrobe.html';
        }
    });

    document.getElementById('loginForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        var messageEl = document.getElementById('message');
        messageEl.textContent = '';
        var payload = {
            username: document.getElementById('username').value.trim(),
            password: document.getElementById('password').value
        };
        var result = await Http.post('/api/auth/login', payload);
        if (result.success) {
            location.href = '/wardrobe/wardrobe.html';
        } else {
            messageEl.textContent = result.errMsg || '登录失败';
        }
    });
})();
