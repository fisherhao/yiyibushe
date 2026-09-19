/**
 * 全站通用 HTTP 封装：基于原生 fetch，统一请求头与响应解析，不引入第三方库。
 * 挂到 window.Http 供各业务页面使用；非 JSON 响应（如未登录被重定向到登录页）返回 null。
 */
(function () {
    async function request(url, options) {
        var response = await fetch(url, options || {});
        var contentType = response.headers.get('Content-Type') || '';
        if (isJsonContentType(contentType)) {
            return response.json();
        }
        return null;
    }

    function isJsonContentType(contentType) {
        return contentType.indexOf('application/json') !== -1;
    }

    window.Http = {
        /** GET 请求 */
        get: function (url) {
            return request(url);
        },
        /** POST JSON 请求 */
        post: function (url, body) {
            return request(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body || {})
            });
        },
        /** POST 表单（文件上传，不要手动设置 Content-Type） */
        postForm: function (url, formData) {
            return request(url, {method: 'POST', body: formData});
        },
        /** DELETE 请求 */
        del: function (url) {
            return request(url, {method: 'DELETE'});
        }
    };
})();
