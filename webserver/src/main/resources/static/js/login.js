//# sourceURL=login.js

function login() {
    $.ajax({
        url: "/login",
        type: "POST",
        data: $("#loginForm").serialize()
    });
}
