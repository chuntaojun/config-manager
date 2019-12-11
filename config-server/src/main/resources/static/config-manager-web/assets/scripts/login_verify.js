$(function() {
    if (sessionStorage.getItem("access_token") == "" || sessionStorage.getItem("access_token") == null) {
        var a = confirm("您当前尚未登陆系统，是否前去登陆")
        if (a) {
            window.location.href = "page-login.html"
        } else {
            window.close()
        }
    }
})