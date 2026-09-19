/**
 * 找回密码页脚本：提交用户名与新密码，成功后回登录页。
 */
document.getElementById('resetForm').addEventListener('submit', async function (event) {
    event.preventDefault();
    var username = document.getElementById('username').value.trim();
    var newPassword = document.getElementById('newPassword').value;
    var messageEl = document.getElementById('message');
    messageEl.textContent = '提交中...';
    var result = await Http.post('/api/auth/reset-password', {
        username: username,
        newPassword: newPassword
    });
    if (result.success) {
        messageEl.textContent = result.data;
        setTimeout(function () {
            location.href = '/auth/login.html';
        }, 1000);
    } else {
        messageEl.textContent = result.errMsg;
    }
});
