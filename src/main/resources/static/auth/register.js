/**
 * 注册页脚本：提交用户名、密码与昵称，成功后进入衣橱主页。
 */
(function () {
    document.getElementById('registerForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        var messageEl = document.getElementById('message');
        messageEl.textContent = '';
        var payload = {
            username: document.getElementById('username').value.trim(),
            password: document.getElementById('password').value,
            nickname: document.getElementById('nickname').value.trim()
        };
        var result = await Http.post('/api/auth/register', payload);
        if (result.success) {
            location.href = '/wardrobe/wardrobe.html';
        } else {
            messageEl.textContent = result.errMsg || '注册失败';
        }
    });
})();
