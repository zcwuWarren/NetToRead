// /* bookshelfBySubCategory.js */
/*=============infinite scroll===================*/

document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const subCategory = urlParams.get('subCategory');

    const likesButton = document.getElementById('switch-likes');
    const collectsButton = document.getElementById('switch-collects');
    const containerCategory = document.getElementById('container-category');
    const loadingContainer = document.querySelector('.loading-container');
    let isLoading = false;
    let currentApi = `/api/bookPage/latest-likes-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`;
    let offset = 0;
    const limit = 50;
    let isEndOfData = false;

    if (!subCategory) {
        console.error("Missing subCategory in URL.");
        return;
    }

    // 預設顯示 likes 書籍
    loadMoreBooks();

    // 點擊 "Likes" 按鈕時，重置並載入最新的 likes 書籍
    likesButton.addEventListener('click', () => {
        resetBookshelf(`/api/bookPage/latest-likes-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
    });

    // 點擊 "Collects" 按鈕時，重置並載入最新的 collects 書籍
    collectsButton.addEventListener('click', () => {
        resetBookshelf(`/api/bookPage/latest-collect-by-subCategory?subCategory=${encodeURIComponent(subCategory)}`);
    });

    // 監聽滾動事件
    containerCategory.addEventListener('scroll', () => {
        if (isNearRight(containerCategory) && !isLoading && !isEndOfData) {
            loadMoreBooks();
        }
    });

    // 重置書架
    function resetBookshelf(apiUrl) {
        currentApi = apiUrl;
        offset = 0;
        isEndOfData = false;
        containerCategory.innerHTML = '';
        showLoading();
        loadMoreBooks();
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

    // 加載更多書籍
    function loadMoreBooks() {
        if (isLoading || isEndOfData) return;
        isLoading = true;
        showLoading();

        fetch(`${currentApi}&offset=${offset}&limit=${limit}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(books => {
                if (books.length > 0) {
                    renderBooks(books);
                    offset += books.length;
                    isLoading = false;

                    if (books.length < limit) {
                        isEndOfData = true;
                    }
                } else {
                    isEndOfData = true;
                    isLoading = false;
                }
                hideLoading();
            })
            .catch(error => {
                console.error("Error fetching books:", error);
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

    // 檢查是否接近右邊
    function isNearRight(element) {
        return element.scrollLeft + element.clientWidth >= element.scrollWidth - 100;
    }
});