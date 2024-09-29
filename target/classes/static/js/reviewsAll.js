// /* copy from bookIdDetail */
// // container b
// document.addEventListener("DOMContentLoaded", async function() {
//     let currentPage = 1;
//     const commentsPerPage = 6;
//     let totalComments = [];
//     let totalQuotes = [];
//
//     const commentsContainer = document.getElementById('containerB');
//     const paginationContainer = document.getElementById('pagination');
//     const bLeftButton = document.getElementById('b-left');
//     const bRightButton = document.getElementById('b-right');
//     let currentLoadFunction = loadComments; // 追踪當前的加載函數，默認為評論加載
//
//
//     // 加載評論
//     async function loadComments(page) {
//         try {
//             const urlParams = new URLSearchParams(window.location.search);
//             const bookId = urlParams.get('bookId');
//             const response = await fetch(`/api/bookPage/latest-comments`);
//             totalComments = await response.json();
//             console.log(totalComments);  // 檢查 API 返回的數據
//             console.log("Book ID: ", bookId);
//
//             // 計算總頁數
//             const totalPages = Math.ceil(totalComments.length / commentsPerPage);
//
//             // 分頁顯示
//             const start = (page - 1) * commentsPerPage;
//             const end = page * commentsPerPage;
//             const commentsToShow = totalComments.slice(start, end);
//
//             renderComments(commentsToShow);
//             renderPagination(totalPages, page, loadComments);  // 修改這行，傳入正確的加載函數
//         } catch (error) {
//             console.error("無法載入評論：", error);
//         }
//     }
//
//     // 加載引用
//     async function loadQuotes(page) {
//         try {
//             const urlParams = new URLSearchParams(window.location.search);
//             const bookId = urlParams.get('bookId');
//             const response = await fetch(`/api/bookPage/latest-quotes`);
//             totalQuotes = await response.json();
//             console.log(totalQuotes); // 檢查 API 返回的數據
//
//             const totalPages = Math.ceil(totalQuotes.length / commentsPerPage);
//             const start = (page - 1) * commentsPerPage;
//             const end = page * commentsPerPage;
//             const quotesToShow = totalQuotes.slice(start, end);
//
//             renderQuotes(quotesToShow);
//             renderPagination(totalPages, page, loadQuotes);
//         } catch (error) {
//             console.error("無法載入引用：", error);
//         }
//     }
//
//     // 渲染評論
//     function renderComments(comments) {
//         const commentsContainer = document.getElementById('containerB');
//         // //also work
//         // const commentsContainer = document.querySelector('.container-b');
//
//         commentsContainer.innerHTML = "";  // 清空現有評論
//
//         comments.forEach(comment => {
//             const commentDiv = document.createElement('div');
//             commentDiv.classList.add('comment-container');
//
//             // 點擊 comment 跳轉至該 bookId 書籍頁面
//             commentDiv.addEventListener('click', function (){
//                 window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
//             });
//
//             const commentText = document.createElement('div');
//             commentText.classList.add('comment-text');
//             commentText.textContent = comment.comment;  // 顯示評論文字
//
//             const bookName = document.createElement('div');
//             bookName.classList.add('comment-book-name');
//             bookName.textContent = comment.bookName;  // 顯示書名
//
//             commentDiv.appendChild(commentText);
//             commentDiv.appendChild(bookName);
//             commentsContainer.appendChild(commentDiv);
//         });
//     }
//
//     // 渲染引用
//     function renderQuotes(quotes) {
//         commentsContainer.innerHTML = "";  // 清空現有引用
//
//         quotes.forEach(quote => {
//             const quoteDiv = document.createElement('div');
//             quoteDiv.classList.add('quote-container');
//
//             // 點擊 quote 跳轉至該 bookId 書籍頁面
//             quoteDiv.addEventListener('click', function (){
//                 window.location.href = `/bookDetail.html?bookId=${quote.bookId}`;
//             });
//
//             const quoteText = document.createElement('div');
//             quoteText.classList.add('quote-text');
//             quoteText.textContent = quote.quote;  // 顯示引用文字
//
//             // const userIdDiv = document.createElement('div');
//             // userIdDiv.classList.add('quote-user-id');
//             // userIdDiv.textContent = quote.userId;  // 顯示用戶 ID
//
//             const bookName = document.createElement('div');
//             bookName.classList.add('quote-book-name');
//             bookName.textContent = quote.bookName;  // 顯示書名
//
//             quoteDiv.appendChild(quoteText);
//             // quoteDiv.appendChild(userIdDiv);
//             quoteDiv.appendChild(bookName);
//             commentsContainer.appendChild(quoteDiv);
//         });
//     }
//
//     // 渲染分頁按鈕
//     function renderPagination(totalPages, currentPage) {
//         const paginationContainer = document.getElementById('pagination');
//         paginationContainer.innerHTML = "";
//
//         for (let i = 1; i <= totalPages; i++) {
//             const button = document.createElement('button');
//             button.textContent = i;
//             button.disabled = i === currentPage;
//
//             button.addEventListener('click', () => {
//                 currentPage = i;
//                 loadComments(currentPage);  // 重新加載對應頁的評論
//             });
//
//             paginationContainer.appendChild(button);
//         }
//     }
//
//
//     // 預設加載評論
//     loadComments(currentPage);
//
//     // 切換到評論
//     bLeftButton.addEventListener('click', () => {
//         currentPage = 1;
//         loadComments(currentPage);
//     });
//
//     // 切換到引用
//     bRightButton.addEventListener('click', () => {
//         currentPage = 1;
//         loadQuotes(currentPage);
//     });
// });
//
//
//
//
//
//

// document.addEventListener("DOMContentLoaded", async function() {
//     let offset = 0;
//     const limit = 10;
//     let isLoading = false;
//     let hasMoreComments = true;
//
//     const commentsContainer = document.getElementById('containerB');
//     const bLeftButton = document.getElementById('b-left');
//     const bRightButton = document.getElementById('b-right');
//     let currentLoadFunction = loadComments;
//
//     async function loadComments() {
//         if (isLoading || !hasMoreComments) return;
//         isLoading = true;
//
//         try {
//             const response = await fetch(`/api/bookPage/latest-comments?offset=${offset}&limit=${limit}`);
//             const comments = await response.json();
//
//             if (comments.length > 0) {
//                 renderComments(comments);
//                 offset += comments.length;
//                 if (comments.length < limit) {
//                     hasMoreComments = false;
//                 }
//             } else {
//                 hasMoreComments = false;
//             }
//         } catch (error) {
//             console.error("無法載入評論：", error);
//         } finally {
//             isLoading = false;
//         }
//     }
//
//     function renderComments(comments) {
//         comments.forEach(comment => {
//             const commentDiv = document.createElement('div');
//             commentDiv.classList.add('comment-container');
//
//             commentDiv.addEventListener('click', function() {
//                 window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
//             });
//
//             const commentText = document.createElement('div');
//             commentText.classList.add('comment-text');
//             commentText.textContent = comment.comment;
//
//             const bookName = document.createElement('div');
//             bookName.classList.add('comment-book-name');
//             bookName.textContent = comment.bookName;
//
//             commentDiv.appendChild(commentText);
//             commentDiv.appendChild(bookName);
//             commentsContainer.appendChild(commentDiv);
//         });
//     }
//
//     function handleScroll() {
//         const scrollPosition = window.innerHeight + window.pageYOffset;
//         const containerBottom = commentsContainer.offsetTop + commentsContainer.offsetHeight;
//
//         if (scrollPosition >= containerBottom - 200 && !isLoading && hasMoreComments) {
//             loadComments();
//         }
//     }
//
//     window.addEventListener('scroll', handleScroll);
//
//     // 初始加載評論
//     loadComments();
//
//     // 切換到評論
//     bLeftButton.addEventListener('click', () => {
//         offset = 0;
//         hasMoreComments = true;
//         commentsContainer.innerHTML = '';
//         loadComments();
//     });
//
//     // 切換到引用（需要實現類似的邏輯）
//     bRightButton.addEventListener('click', () => {
//         // 實現引用加載邏輯
//     });
// });

document.addEventListener("DOMContentLoaded", async function() {
    let commentOffset = 0;
    let quoteOffset = 0;
    const limit = 10;
    let isLoading = false;
    let hasMoreComments = true;
    let hasMoreQuotes = true;

    const containerB = document.getElementById('containerB');
    const bLeftButton = document.getElementById('b-left');
    const bRightButton = document.getElementById('b-right');
    let currentLoadFunction = loadComments;

    async function loadComments() {
        if (isLoading || !hasMoreComments) return;
        isLoading = true;

        try {
            const response = await fetch(`/api/bookPage/latest-comments?offset=${commentOffset}&limit=${limit}`);
            const comments = await response.json();

            if (comments.length > 0) {
                renderComments(comments);
                commentOffset += comments.length;
                if (comments.length < limit) {
                    hasMoreComments = false;
                }
            } else {
                hasMoreComments = false;
            }
        } catch (error) {
            console.error("無法載入評論：", error);
        } finally {
            isLoading = false;
        }
    }

    async function loadQuotes() {
        if (isLoading || !hasMoreQuotes) return;
        isLoading = true;

        try {
            const response = await fetch(`/api/bookPage/latest-quotes?offset=${quoteOffset}&limit=${limit}`);
            const quotes = await response.json();

            if (quotes.length > 0) {
                renderQuotes(quotes);
                quoteOffset += quotes.length;
                if (quotes.length < limit) {
                    hasMoreQuotes = false;
                }
            } else {
                hasMoreQuotes = false;
            }
        } catch (error) {
            console.error("無法載入引用：", error);
        } finally {
            isLoading = false;
        }
    }

    function renderComments(comments) {
        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment-container');

            commentDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
            });

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = comment.bookName;

            commentDiv.appendChild(commentText);
            commentDiv.appendChild(bookName);
            containerB.appendChild(commentDiv);
        });
    }

    function renderQuotes(quotes) {
        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            quoteDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${quote.bookId}`;
            });

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;

            const bookName = document.createElement('div');
            bookName.classList.add('quote-book-name');
            bookName.textContent = quote.bookName;

            quoteDiv.appendChild(quoteText);
            quoteDiv.appendChild(bookName);
            containerB.appendChild(quoteDiv);
        });
    }

    function handleScroll() {
        const scrollPosition = window.innerHeight + window.pageYOffset;
        const containerBottom = containerB.offsetTop + containerB.offsetHeight;

        if (scrollPosition >= containerBottom - 200 && !isLoading) {
            currentLoadFunction();
        }
    }

    window.addEventListener('scroll', handleScroll);

    // 初始加載評論
    loadComments();

    // 切換到評論
    bLeftButton.addEventListener('click', () => {
        containerB.innerHTML = '';
        commentOffset = 0;
        hasMoreComments = true;
        currentLoadFunction = loadComments;
        loadComments();
    });

    // 切換到引用
    bRightButton.addEventListener('click', () => {
        containerB.innerHTML = '';
        quoteOffset = 0;
        hasMoreQuotes = true;
        currentLoadFunction = loadQuotes;
        loadQuotes();
    });
});