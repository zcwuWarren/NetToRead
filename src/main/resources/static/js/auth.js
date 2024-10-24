document.addEventListener("DOMContentLoaded", function() {
    const token = localStorage.getItem('jwtToken');
    const loginLogoutElement = document.querySelector('.profile-box .profile-item:last-child');

    function isTokenExpired(token) {
        if (!token) return true;
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return true;  // invalid token
        }

        const decodedPayload = JSON.parse(atob(tokenParts[1]));
        const expirationDate = new Date(decodedPayload.exp * 1000);
        const now = new Date();

        return expirationDate < now;
    }

    function updateLoginLogoutButton() {
        if (token && !isTokenExpired(token)) {
            loginLogoutElement.innerHTML = '<a href="#" id="logoutButton">登出</a>';
            document.getElementById('logoutButton').addEventListener('click', function(e) {
                e.preventDefault();
                localStorage.removeItem('jwtToken');
                window.location.reload();
            });
        } else {
            loginLogoutElement.innerHTML = '<a href="account.html">登入 / 註冊</a>';
        }
    }

    // initial button status
    updateLoginLogoutButton();

    // check and handle token
    if (token) {
        if (isTokenExpired(token)) {
            console.error("JWT Token has expired.");
            localStorage.removeItem('jwtToken');
            updateLoginLogoutButton();
        }
    }
});
