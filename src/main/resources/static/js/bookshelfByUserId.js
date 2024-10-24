// // bookshelfByUserId.js
document.addEventListener("DOMContentLoaded", function() {
    const containerCategory = document.getElementById('container-category');
    const token = localStorage.getItem('jwtToken');
    const loadingContainer = document.querySelector('.loading-container');
    const bookshelfReviewTitleLikeCollect = document.getElementById('bookshelfReviewTitleLikeCollect').querySelector('h2');


    let offset = 0;
    const limit = 50; // 每次加載的書籍數量
    let isLoading = false;
    let isEndOfData = false;

    // 按鈕元素
    const likesButton = document.getElementById('switch-likes');
    const collectsButton = document.getElementById('switch-collects');

    let currentApi = '/api/userPage/myLike'; // 預設 API

    if (!token) {
        console.error("No token found in localStorage.");
        window.location.href = '/account.html'; // 跳轉到登入頁面
    } else {
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

    // 設置初始 active 狀態
    setActiveState(likesButton);
    updateBookshelfTitle('likes');

    // 預設載入 Likes 書籍
    loadMoreBooks();

    // 點擊 Likes 時載入
    likesButton.addEventListener('click', () => {
        resetBookshelf('/api/userPage/myLike');
        setActiveState(likesButton);
        removeActiveState(collectsButton);
        updateBookshelfTitle('likes');
    });

    // 點擊 Collects 時載入
    collectsButton.addEventListener('click', () => {
        resetBookshelf('/api/userPage/myCollect');
        setActiveState(collectsButton);
        removeActiveState(likesButton);
        updateBookshelfTitle('collects');
    });

    // 設置 active 狀態
    function setActiveState(button) {
        button.classList.add('active');
        button.style.backgroundColor = '#B6ADA5';
        button.style.color = '#041723';
    }

    // 移除 active 狀態
    function removeActiveState(button) {
        button.classList.remove('active');
        button.style.backgroundColor = '';
        button.style.color = '';
    }

    // 更新書架標題
    function updateBookshelfTitle(type) {
        if (type === 'likes') {
            bookshelfReviewTitleLikeCollect.textContent = '我的按讚';
        } else if (type === 'collects') {
            bookshelfReviewTitleLikeCollect.textContent = '我的收藏';
        }
    }

    // 顯示加載動畫
    function showLoading() {
        loadingContainer.style.display = 'flex';
        containerCategory.classList.remove('loaded');
    }

    // 隱藏加載動畫
    function hideLoading() {
        loadingContainer.style.display = 'none';
        containerCategory.classList.add('loaded');
    }

    // 重置書架
    function resetBookshelf(apiUrl) {
        currentApi = apiUrl;
        offset = 0;
        isEndOfData = false;
        containerCategory.innerHTML = '';
        showLoading();
        loadMoreBooks();
    }

    // 加載更多書籍
    function loadMoreBooks() {
        if (isLoading || isEndOfData) return;
        isLoading = true;
        showLoading();

        fetch(`${currentApi}?offset=${offset}&limit=${limit}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ token: token })
        })
            .then(response => response.json())
            .then(books => {
                if (books.length > 0) {
                    renderBooks(books);
                    offset += books.length;
                    if (books.length < limit) {
                        isEndOfData = true;
                    }
                } else {
                    isEndOfData = true;
                }
                hideLoading();
                isLoading = false;
            })
            .catch(error => {
                console.error(`Error fetching books from ${currentApi}:`, error);
                isLoading = false;
                hideLoading();
            });
    }

    // 渲染書籍 enlarge
    function renderBooks(books) {
        books.forEach(book => {
            const bookDiv = document.createElement('div');
            bookDiv.classList.add('book-container');

            const bookCover = document.createElement('img');
            bookCover.src = book.bookCover;
            bookCover.alt = book.bookName;
            bookCover.classList.add('book-cover');

            bookDiv.appendChild(bookCover);

            // 添加鼠標懸停事件
            bookDiv.addEventListener('mouseenter', function() {
                bookNameDisplay.textContent = book.bookName;
                bookNameDisplay.style.opacity = '1';
            });

            // 添加鼠標離開事件
            bookDiv.addEventListener('mouseleave', function() {
                bookNameDisplay.style.opacity = '0';
            });

            bookDiv.addEventListener('click', function () {
                window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
            });

            containerCategory.appendChild(bookDiv);
        });
    }


    // 監聽滾動事件
    containerCategory.addEventListener('scroll', () => {
        if (isNearRight(containerCategory) && !isLoading && !isEndOfData) {
            loadMoreBooks();
        }
    });

    // 檢查是否接近右邊
    function isNearRight(element) {
        return element.scrollLeft + element.clientWidth >= element.scrollWidth - 100;
    }
});