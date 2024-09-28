// bookshelfByUserId.js

document.addEventListener("DOMContentLoaded", function() {
    const booksPerPage = 6;  // 每頁顯示 6 本書
    let currentPage = 1;
    let totalBooks = [];
    const containerCategory = document.getElementById('container-category');
    const paginationContainer = document.getElementById('pagination-bookshelf');
    const token = localStorage.getItem('jwtToken');

    // 按鈕元素
    const likesButton = document.getElementById('switch-likes');
    const collectsButton = document.getElementById('switch-collects');

    if (!token) {
        console.error("No token found in localStorage.");
        window.location.href = '/account.html';}  // 跳轉到登入頁面
        else {
        document.body.style.display = "block";
    }

    // 檢查 JWT Token 是否過期
    function isTokenExpired(token) {
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            return true;  // 無效的 token
        }

        const decodedPayload = JSON.parse(atob(tokenParts[1]));
        const expirationDate = new Date(decodedPayload.exp * 1000);  // exp 是以秒計算的
        const now = new Date();

        return expirationDate < now;  // 如果當前時間大於過期時間，token 已過期
    }

    if (isTokenExpired(token)) {
        console.error("JWT Token has expired.");
        localStorage.removeItem('jwtToken');  // 清除過期的 token
        window.location.href = '/account.html';  // 跳轉到登入頁面
        return;
    }

    // 如果 Token 有效，繼續執行頁面渲染邏輯
    // 預設載入 Likes 書籍
    loadBookshelf('/api/userPage/myLike');

    // 點擊 Likes 時載入
    likesButton.addEventListener('click', () => {
        loadBookshelf('/api/userPage/myLike');
    });

    // 點擊 Collects 時載入
    collectsButton.addEventListener('click', () => {
        loadBookshelf('/api/userPage/myCollect');
    });

    // 通用載入書籍的函數
    function loadBookshelf(apiUrl) {
        fetch(apiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ token: token })
        })
            .then(response => response.json())
            .then(books => {
                totalBooks = books;  // 保存所有書籍
                renderBooks(currentPage);  // 初始加載當前頁面書籍
                renderPagination();  // 渲染分頁按鈕
            })
            .catch(error => console.error(`Error fetching books from ${apiUrl}:`, error));
    }

    // 渲染當前頁面的書籍
    function renderBooks(page) {
        containerCategory.innerHTML = '';  // 清空之前的內容

        const start = (page - 1) * booksPerPage;
        const end = page * booksPerPage;
        const booksToShow = totalBooks.slice(start, end);  // 根據當前頁數提取書籍

        // 如果沒有書籍，顯示提示
        if (booksToShow.length === 0) {
            containerCategory.innerHTML = '<p>No books found.</p>';
            return;
        }

        // 渲染書籍
        booksToShow.forEach(book => {
            const bookDiv = document.createElement('div');
            bookDiv.classList.add('book-container');

            // 添加點擊事件跳轉到 bookDetail 頁面
            bookDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
            });

            bookDiv.innerHTML = `
                <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
                <div class="book-name">${book.bookName}</div>
            `;
            containerCategory.appendChild(bookDiv);
        });
    }

    // 渲染分頁按鈕
    function renderPagination() {
        paginationContainer.innerHTML = '';  // 清空現有分頁按鈕

        const totalPages = Math.ceil(totalBooks.length / booksPerPage);  // 計算總頁數

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;  // 當前頁面按鈕禁用

            button.addEventListener('click', function() {
                currentPage = i;  // 更新當前頁數
                renderBooks(currentPage);  // 重新渲染當前頁面的書籍
                renderPagination(); // 重新渲染分頁按鈕
            });

            paginationContainer.appendChild(button);
        }
    }
});
