// container a
document.addEventListener("DOMContentLoaded", async function() {
    // 從 URL 中提取 bookId
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');

    if (bookId) {
        try {
            const response = await fetch(`/api/bookPage/getBookInfo?bookId=${bookId}`);
            const book = await response.json();

            const containerA1 = document.getElementById('containerA1');
            const containerA2 = document.getElementById('containerA2');
            const containerA3 = document.getElementById('containerA3');
            const likeButton = document.getElementById('A-2-like');
            const collectButton = document.getElementById('A-2-collect');

            // 渲染書籍封面
            const bookCover = document.createElement('img');
            bookCover.src = book.bookCover;
            containerA1.appendChild(bookCover);

            // 渲染書籍資訊
            const bookInfo = `
                <div><strong>Book Name:</strong> ${book.bookName}</div>
                <div><strong>Author:</strong> ${book.author}</div>
                <div><strong>Publisher:</strong> ${book.publisher}</div>
                <div><strong>Publish Date:</strong> ${book.publishDate}</div>
                <div><strong>ISBN:</strong> ${book.isbn}</div>
                <div><strong>Likes:</strong> ${book.like}</div>
                <div><strong>Collects:</strong> ${book.collect}</div>
                <div><strong>Content:</strong> ${book.content}</div>
            `;
            containerA2.innerHTML = bookInfo;

            // 渲染書籍描述
            const bookDescription = `<div class="container-a-3-long-content">${book.description}</div>`;
            containerA3.innerHTML = bookDescription;

            // 按讚功能
            likeButton.addEventListener('click', async () => {
                const token = localStorage.getItem('jwtToken');
                if (!token) {
                    alert("Please log in to like the book.");
                    window.location.href = "account.html";
                    return;
                }

                try {
                    const response = await fetch(`/api/book/likeBook?bookId=${bookId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ token })
                    });
                    const result = await response.json();
                    if (response.ok) {
                        alert(result.message);
                    } else {
                        alert(result.message);
                    }
                } catch (error) {
                    console.error("Error liking the book:", error);
                }
            });

            // 收藏功能
            collectButton.addEventListener('click', async () => {
                const token = localStorage.getItem('jwtToken');
                if (!token) {
                    alert("Please log in to collect the book.");
                    window.location.href = "account.html";
                    return;
                }

                try {
                    const response = await fetch(`/api/book/collectBook?bookId=${bookId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ token })
                    });
                    const result = await response.json();
                    if (response.ok) {
                        alert(result.message);
                    } else {
                        alert(result.message);
                    }
                } catch (error) {
                    console.error("Error collecting the book:", error);
                }
            });

        } catch (error) {
            console.error("無法加載書籍詳細信息：", error);
        }
    } else {
        console.error("缺少 bookId");
    }
});

// container b
document.addEventListener("DOMContentLoaded", async function() {
    let currentPage = 1;
    const commentsPerPage = 6;
    let totalComments = [];
    let totalQuotes = [];

    const commentsContainer = document.getElementById('containerB');
    const paginationContainer = document.getElementById('pagination');
    const bLeftButton = document.getElementById('b-left');
    const bRightButton = document.getElementById('b-right');
    let currentLoadFunction = loadComments; // 追踪當前的加載函數，默認為評論加載


    // 加載評論
    async function loadComments(page) {
        try {
            const urlParams = new URLSearchParams(window.location.search);
            const bookId = urlParams.get('bookId');
            const response = await fetch(`/api/bookPage/switchToComment?bookId=${bookId}`);
            totalComments = await response.json();
            console.log(totalComments);  // 檢查 API 返回的數據
            console.log("Book ID: ", bookId);

            // 計算總頁數
            const totalPages = Math.ceil(totalComments.length / commentsPerPage);

            // 分頁顯示
            const start = (page - 1) * commentsPerPage;
            const end = page * commentsPerPage;
            const commentsToShow = totalComments.slice(start, end);

            renderComments(commentsToShow);
            renderPagination(totalPages, page, loadComments);  // 修改這行，傳入正確的加載函數
        } catch (error) {
            console.error("無法載入評論：", error);
        }
    }

    // 加載引用
    async function loadQuotes(page) {
        try {
            const urlParams = new URLSearchParams(window.location.search);
            const bookId = urlParams.get('bookId');
            const response = await fetch(`/api/bookPage/switchToQuote?bookId=${bookId}`);
            totalQuotes = await response.json();
            console.log(totalQuotes); // 檢查 API 返回的數據

            const totalPages = Math.ceil(totalQuotes.length / commentsPerPage);
            const start = (page - 1) * commentsPerPage;
            const end = page * commentsPerPage;
            const quotesToShow = totalQuotes.slice(start, end);

            renderQuotes(quotesToShow);
            renderPagination(totalPages, page, loadQuotes);
        } catch (error) {
            console.error("無法載入引用：", error);
        }
    }

    // 渲染評論
    function renderComments(comments) {
        const commentsContainer = document.getElementById('containerB');
        commentsContainer.innerHTML = "";  // 清空現有評論

        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment-container');

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;  // 顯示評論文字

            const userIdDiv = document.createElement('div');
            userIdDiv.classList.add('comment-user-id');
            userIdDiv.textContent = comment.userId;  // 顯示用戶 ID

            commentDiv.appendChild(commentText);
            commentDiv.appendChild(userIdDiv);
            commentsContainer.appendChild(commentDiv);
        });
    }

    // 渲染引用
    function renderQuotes(quotes) {
        commentsContainer.innerHTML = "";  // 清空現有引用

        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;  // 顯示引用文字

            const userIdDiv = document.createElement('div');
            userIdDiv.classList.add('quote-user-id');
            userIdDiv.textContent = quote.userId;  // 顯示用戶 ID

            quoteDiv.appendChild(quoteText);
            quoteDiv.appendChild(userIdDiv);
            commentsContainer.appendChild(quoteDiv);
        });
    }

    // 渲染分頁按鈕
    function renderPagination(totalPages, currentPage) {
        const paginationContainer = document.getElementById('pagination');
        paginationContainer.innerHTML = "";

        for (let i = 1; i <= totalPages; i++) {
            const button = document.createElement('button');
            button.textContent = i;
            button.disabled = i === currentPage;

            button.addEventListener('click', () => {
                currentPage = i;
                loadComments(currentPage);  // 重新加載對應頁的評論
            });

            paginationContainer.appendChild(button);
        }
    }


    // 預設加載評論
    loadComments(currentPage);

    // 切換到評論
    bLeftButton.addEventListener('click', () => {
        currentPage = 1;
        loadComments(currentPage);
    });

    // 切換到引用
    bRightButton.addEventListener('click', () => {
        currentPage = 1;
        loadQuotes(currentPage);
    });
});

// container C
document.addEventListener("DOMContentLoaded", function() {
    const switchCommentButton = document.getElementById('switch-comment');
    const switchQuoteButton = document.getElementById('switch-quote');
    const submitButton = document.getElementById('submit-button');
    const inputBox = document.getElementById('input-box');

    let currentMode = 'comment'; // 預設為 Comment 模式

    // 切換至 Comment 模式
    switchCommentButton.addEventListener('click', () => {
        currentMode = 'comment';
        inputBox.placeholder = "Enter your comment...";
    });

    // 切換至 Quote 模式
    switchQuoteButton.addEventListener('click', () => {
        currentMode = 'quote';
        inputBox.placeholder = "Enter your quote...";
    });

    // 點擊送出按鈕
    submitButton.addEventListener('click', async () => {
        const token = localStorage.getItem('jwtToken'); // 取得 localStorage 中的 jwtToken
        const commentOrQuote = inputBox.value.trim(); // 取得輸入框的內容
        const urlParams = new URLSearchParams(window.location.search);
        const bookId = urlParams.get('bookId'); // 從 URL 取得 bookId

        if (!token) {
            alert("Please log in to submit.");
            window.location.href = "account.html"; // 跳轉至登入頁面
            return;
        }

        if (commentOrQuote === "") {
            alert("Input cannot be empty.");
            return;
        }

        try {
            let apiUrl;
            let requestBody;

            // 根據當前模式設定 API 和 Request Body
            if (currentMode === 'comment') {
                apiUrl = `/api/book/addComment`;
                requestBody = { comment: commentOrQuote, token: token };
            } else {
                apiUrl = `/api/book/addQuote`;
                requestBody = { quote: commentOrQuote, token: token };
            }

            const response = await fetch(`${apiUrl}?bookId=${bookId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });

            const result = await response.json();

            if (response.ok) {
                alert(result.message);
                inputBox.value = ""; // 清空輸入框
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("Error submitting data: ", error);
            alert("Failed to submit.");
        }
    });
});