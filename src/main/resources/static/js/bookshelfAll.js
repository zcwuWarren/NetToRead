// // // /* bookshelfAll.js */
// /*=================================*/
// document.addEventListener("DOMContentLoaded", function () {
//     const likesButton = document.getElementById('switch-likes');
//     const collectsButton = document.getElementById('switch-collects');
//     const latestLikesContainer = document.getElementById('latestLikesContainer');
//     const paginationContainer = document.getElementById('pagination-bookshelf');
//     const booksPerPage = 6;
//     let currentPage = 1;
//     let totalBooks = [];
//
//     // 預設顯示 likes 書籍
//     loadBookshelf('/api/bookPage/latest-likes');
//
//     // 點擊 "Likes" 按鈕時，載入最新的 likes 書籍
//     likesButton.addEventListener('click', () => {
//         currentPage = 1;  // 每次切換重置到第一頁
//         loadBookshelf('/api/bookPage/latest-likes');
//     });
//
//     // 點擊 "Collects" 按鈕時，載入最新的 collects 書籍
//     collectsButton.addEventListener('click', () => {
//         currentPage = 1;  // 每次切換重置到第一頁
//         loadBookshelf('/api/bookPage/latest-collect');
//     });
//
//     // 加載書籍的函數
//     function loadBookshelf(apiUrl) {
//         fetch(apiUrl)
//             .then(response => response.json())
//             .then(books => {
//                 totalBooks = books;  // 保存所有書籍，用於分頁
//                 renderBooks(currentPage);  // 顯示當前頁的書籍
//                 renderPagination();  // 渲染分頁按鈕
//             })
//             .catch(error => console.error("Error fetching books:", error));
//     }
//
//     // 渲染當前頁的書籍
//     function renderBooks(page) {
//         latestLikesContainer.innerHTML = '';  // 清空之前的內容
//
//         const start = (page - 1) * booksPerPage;
//         const end = page * booksPerPage;
//         const booksToShow = totalBooks.slice(start, end);
//
//         // 如果沒有書籍，顯示提示
//         if (booksToShow.length === 0) {
//             latestLikesContainer.innerHTML = '<p>No books found.</p>';
//             return;
//         }
//
//         // 渲染書籍
//         booksToShow.forEach(book => {
//             const bookDiv = document.createElement('div');
//             bookDiv.classList.add('book-container');
//
//             // 設置點擊事件跳轉到 bookDetail 頁面
//             bookDiv.addEventListener('click', function () {
//                 window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
//             });
//
//             bookDiv.innerHTML = `
//                 <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
//                 <div class="book-name">${book.bookName}</div>
//             `;
//             latestLikesContainer.appendChild(bookDiv);
//         });
//     }
//
//     // 渲染分頁按鈕
//     function renderPagination() {
//         paginationContainer.innerHTML = '';  // 清空現有分頁按鈕
//
//         const totalPages = Math.ceil(totalBooks.length / booksPerPage);
//
//         for (let i = 1; i <= totalPages; i++) {
//             const button = document.createElement('button');
//             button.textContent = i;
//             button.disabled = i === currentPage;  // 當前頁按鈕禁用
//
//             button.addEventListener('click', () => {
//                 currentPage = i;  // 更新當前頁
//                 renderBooks(currentPage);  // 渲染當前頁書籍
//                 renderPagination();  // 重新渲染分頁按鈕，正確更新按鈕狀態
//             });
//
//             paginationContainer.appendChild(button);
//         }
//     }
// });

// // bookshelfAll.js
// document.addEventListener("DOMContentLoaded", function () {
//     const likesButton = document.getElementById('switch-likes');
//     const collectsButton = document.getElementById('switch-collects');
//     const latestLikesContainer = document.getElementById('latestLikesContainer');
//     let isLoading = false;
//     let currentApi = '/api/bookPage/latest-likes';
//     let offset = 0;
//     const booksPerLoad = 10;
//
//     // 預設顯示 likes 書籍
//     loadMoreBooks();
//
//     // 點擊 "Likes" 按鈕時，重置並載入最新的 likes 書籍
//     likesButton.addEventListener('click', () => {
//         resetBookshelf('/api/bookPage/latest-likes');
//     });
//
//     // 點擊 "Collects" 按鈕時，重置並載入最新的 collects 書籍
//     collectsButton.addEventListener('click', () => {
//         resetBookshelf('/api/bookPage/latest-collect');
//     });
//
//     // 監聽滾動事件
//     latestLikesContainer.addEventListener('scroll', () => {
//         if (isNearRight(latestLikesContainer) && !isLoading) {
//             loadMoreBooks();
//         }
//     });
//
//     // 重置書架
//     function resetBookshelf(apiUrl) {
//         currentApi = apiUrl;
//         offset = 0;
//         latestLikesContainer.innerHTML = '';
//         loadMoreBooks();
//     }
//
//     // 加載更多書籍
//     function loadMoreBooks() {
//         if (isLoading) return;
//         isLoading = true;
//
//         fetch(`${currentApi}?offset=${offset}&limit=${booksPerLoad}`)
//             .then(response => response.json())
//             .then(books => {
//                 renderBooks(books);
//                 offset += books.length;
//                 isLoading = false;
//             })
//             .catch(error => {
//                 console.error("Error fetching books:", error);
//                 isLoading = false;
//             });
//     }
//
//     // 渲染書籍
//     function renderBooks(books) {
//         books.forEach(book => {
//             const bookDiv = document.createElement('div');
//             bookDiv.classList.add('book-container');
//
//             bookDiv.addEventListener('click', function () {
//                 window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
//             });
//
//             bookDiv.innerHTML = `
//                 <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
//                 <div class="book-name">${book.bookName}</div>
//             `;
//             latestLikesContainer.appendChild(bookDiv);
//         });
//     }
//
//     // 檢查是否接近右邊
//     function isNearRight(element) {
//         return element.scrollLeft + element.clientWidth >= element.scrollWidth - 100;
//     }
// });

// // bookshelfAll.js
// document.addEventListener("DOMContentLoaded", function () {
//     const likesButton = document.getElementById('switch-likes');
//     const collectsButton = document.getElementById('switch-collects');
//     const latestLikesContainer = document.getElementById('latestLikesContainer');
//     let isLoading = false;
//     let currentApi = '/api/bookPage/latest-likes';
//     let offset = 0;
//     const limit = 10;
//     let isEndOfData = false;
//
//     // 預設顯示 likes 書籍
//     loadMoreBooks();
//
//     // 點擊 "Likes" 按鈕時，重置並載入最新的 likes 書籍
//     likesButton.addEventListener('click', () => {
//         resetBookshelf('/api/bookPage/latest-likes');
//     });
//
//     // 點擊 "Collects" 按鈕時，重置並載入最新的 collects 書籍
//     collectsButton.addEventListener('click', () => {
//         resetBookshelf('/api/bookPage/latest-collect');
//     });
//
//     // 監聽滾動事件
//     latestLikesContainer.addEventListener('scroll', () => {
//         if (isNearRight(latestLikesContainer) && !isLoading) {
//             loadMoreBooks();
//         }
//     });
//
//     // 重置書架
//     function resetBookshelf(apiUrl) {
//         currentApi = apiUrl;
//         offset = 0;
//         isEndOfData = false;
//         latestLikesContainer.innerHTML = '';
//         latestLikesContainer.scrollLeft = 0;
//         loadMoreBooks();
//     }
//
//     // 加載更多書籍
//     function loadMoreBooks() {
//         if (isLoading) return;
//         isLoading = true;
//
//         fetch(`${currentApi}?offset=${offset}&limit=${limit}`)
//             .then(response => response.json())
//             .then(books => {
//                 if (books.length > 0) {
//                     renderBooks(books);
//                     offset += books.length;
//                     isLoading = false;
//
//                     // 如果返回的書籍數量少於 limit，說明已到達數據末尾
//                     if (books.length < limit) {
//                         isEndOfData = true;
//                     }
//                 } else {
//                     // 如果沒有更多書籍，禁用進一步的加載
//                     // latestLikesContainer.removeEventListener('scroll', loadMoreBooks);
//                     isEndOfData = true;
//                     isLoading = false;
//                 }
//             })
//             .catch(error => {
//                 console.error("Error fetching books:", error);
//                 isLoading = false;
//             });
//     }
//
//     // 渲染書籍
//     function renderBooks(books) {
//         books.forEach(book => {
//             const bookDiv = document.createElement('div');
//             bookDiv.classList.add('book-container');
//
//             bookDiv.addEventListener('click', function () {
//                 window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
//             });
//
//             bookDiv.innerHTML = `
//                 <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
//                 <div class="book-name">${book.bookName}</div>
//             `;
//             latestLikesContainer.appendChild(bookDiv);
//         });
//     }
//
//     // 檢查是否接近右邊
//     function isNearRight(element) {
//         return element.scrollLeft + element.clientWidth >= element.scrollWidth - 100;
//     }
// });

// bookshelfAll.js
document.addEventListener("DOMContentLoaded", function () {
    const likesButton = document.getElementById('switch-likes');
    const collectsButton = document.getElementById('switch-collects');
    const latestLikesContainer = document.getElementById('latestLikesContainer');
    let isLoading = false;
    let currentApi = '/api/bookPage/latest-likes';
    let offset = 0;
    const limit = 50;
    let isEndOfData = false;

    // 預設顯示 likes 書籍
    loadMoreBooks();

    // 點擊 "Likes" 按鈕時，重置並載入最新的 likes 書籍
    likesButton.addEventListener('click', () => {
        resetBookshelf('/api/bookPage/latest-likes');
    });

    // 點擊 "Collects" 按鈕時，重置並載入最新的 collects 書籍
    collectsButton.addEventListener('click', () => {
        resetBookshelf('/api/bookPage/latest-collect');
    });

    // 監聽滾動事件
    latestLikesContainer.addEventListener('scroll', () => {
        if (isNearRight(latestLikesContainer) && !isLoading && !isEndOfData) {
            loadMoreBooks();
        }
    });

    // 重置書架
    function resetBookshelf(apiUrl) {
        currentApi = apiUrl;
        offset = 0;
        isEndOfData = false;
        latestLikesContainer.innerHTML = '';
        latestLikesContainer.scrollLeft = 0;
        loadMoreBooks();
    }

    // 加載更多書籍
    function loadMoreBooks() {
        if (isLoading || isEndOfData) return;
        isLoading = true;

        fetch(`${currentApi}?offset=${offset}&limit=${limit}`)
            .then(response => response.json())
            .then(books => {
                if (books.length > 0) {
                    renderBooks(books);
                    offset += books.length;
                    isLoading = false;

                    // 如果返回的書籍數量少於 limit，說明已到達數據末尾
                    if (books.length < limit) {
                        isEndOfData = true;
                    }
                } else {
                    isEndOfData = true;
                    isLoading = false;
                }
            })
            .catch(error => {
                console.error("Error fetching books:", error);
                isLoading = false;
            });
    }

    // 渲染書籍
    function renderBooks(books) {
        books.forEach(book => {
            const bookDiv = document.createElement('div');
            bookDiv.classList.add('book-container');

            bookDiv.addEventListener('click', function () {
                window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
            });

            bookDiv.innerHTML = `
                <img src="${book.bookCover}" alt="${book.bookName}" class="book-cover">
                <div class="book-name">${book.bookName}</div>
            `;
            latestLikesContainer.appendChild(bookDiv);
        });
    }

    // 檢查是否接近右邊
    function isNearRight(element) {
        return element.scrollLeft + element.clientWidth >= element.scrollWidth - 100;
    }

    // 創建並添加 fade 元素
    const fadeLeft = document.createElement('div');
    fadeLeft.className = 'fade-effect fade-left';
    const fadeRight = document.createElement('div');
    fadeRight.className = 'fade-effect fade-right';
    latestLikesContainer.parentNode.insertBefore(fadeLeft, latestLikesContainer);
    latestLikesContainer.parentNode.appendChild(fadeRight);

    // 更新 fade 效果的顯示邏輯
    function updateFadeEffect() {
        const scrollLeft = latestLikesContainer.scrollLeft;
        const scrollWidth = latestLikesContainer.scrollWidth;
        const clientWidth = latestLikesContainer.clientWidth;

        // 控制左側 fade 效果
        if (scrollLeft > 0) {
            fadeLeft.style.opacity = '1';
        } else {
            fadeLeft.style.opacity = '0';
        }

        // 控制右側 fade 效果
        if (scrollLeft + clientWidth < scrollWidth) {
            fadeRight.style.opacity = '1';
        } else {
            fadeRight.style.opacity = '0';
        }
    }

    // 初始化 fade 效果
    updateFadeEffect();

    // 在滾動事件中更新 fade 效果
    latestLikesContainer.addEventListener('scroll', () => {
        updateFadeEffect();
        if (isNearRight(latestLikesContainer) && !isLoading && !isEndOfData) {
            loadMoreBooks();
        }
    });
});

