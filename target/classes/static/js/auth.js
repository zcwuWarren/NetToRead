document.addEventListener("DOMContentLoaded", function() {
    const token = localStorage.getItem('jwtToken');
    const loginLogoutElement = document.querySelector('.profile-box .profile-item:first-child');

    function isTokenExpired(token) {
        if (!token) return true;
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return true;  // 無效的 token
        }

        const decodedPayload = JSON.parse(atob(tokenParts[1]));
        const expirationDate = new Date(decodedPayload.exp * 1000);  // exp 是以秒計算的
        const now = new Date();

        return expirationDate < now;  // 如果當前時間大於過期時間，token 已過期
    }

    function updateLoginLogoutButton() {
        if (token && !isTokenExpired(token)) {
            loginLogoutElement.innerHTML = '<a href="#" id="logoutButton">登出</a>';
            document.getElementById('logoutButton').addEventListener('click', function(e) {
                e.preventDefault();
                localStorage.removeItem('jwtToken');
                window.location.reload(); // 重新加載當前頁面
            });
        } else {
            loginLogoutElement.innerHTML = '<a href="account.html">登入 / 註冊</a>';
        }
    }

    // 初始化按鈕狀態
    updateLoginLogoutButton();

    // 檢查並處理 token
    if (token) {
        if (isTokenExpired(token)) {
            console.error("JWT Token has expired.");
            localStorage.removeItem('jwtToken');
            updateLoginLogoutButton();
        }
    }
});
