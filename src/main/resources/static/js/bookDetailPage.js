// bookDetailPage.js
// container a
document.addEventListener("DOMContentLoaded", async function() {
    // 從 URL 中提取 bookId
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');
    let book;

    if (bookId) {
        try {
            const response = await fetch(`/api/bookPage/getBookInfo?bookId=${bookId}`);
            book = await response.json();

            const containerA1 = document.getElementById('containerA1');
            const containerA2 = document.getElementById('containerA2');
            const containerA3 = document.getElementById('containerA3');
            const likeButton = document.getElementById('like-button');
            const collectButton = document.getElementById('collect-button');

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

            // 圖書館主容器下拉選單查詢功能
            const librarySelect = document.getElementById('library-select');
            if (librarySelect) {
                librarySelect.addEventListener('change', function() {
                    const selectedLibrary = this.value;
                    if (selectedLibrary) {
                        if (book && book.isbn) {
                            const isbn = book.isbn;
                            const searchUrl = selectedLibrary.replace('${isbn}', isbn);
                            console.log('Opening library search URL:', searchUrl);
                            window.open(searchUrl, '_blank');
                        } else {
                            console.error('Book or ISBN not available:', book);
                        }
                    } else {
                        console.log('No library selected');
                    }
                });
            } else {
                console.error("Library select element not found");
            }

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

    const token = localStorage.getItem('jwtToken');
    let userIdFromToken = null;

    // 如果有 JWT，解析用戶 ID
    if (token) {
        const decodedToken = parseJwt(token);
        userIdFromToken = decodedToken.userId; // 解析出 userId
    }

    // 解析 JWT Token 的函數
    function parseJwt(token) {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
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
        return;
    }

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

    // 新增 toggleEdit
    function toggleEdit(textElement, editButton, id, type) {
        const isEditing = textElement.getAttribute('contenteditable') === 'true';
        if (isEditing) {
            saveEdit(textElement, editButton, id, type);
        } else {
            // 開始編輯
            textElement.setAttribute('contenteditable', 'true');
            textElement.focus();
            editButton.innerHTML = `
            <svg viewBox="0 0 24 24" width="24" height="24">
                <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
            </svg>
        `;

            // 添加 keydown 事件監聽器
            textElement.addEventListener('keydown', function(event) {
                if (event.key === 'Enter' && !event.shiftKey) {
                    event.preventDefault();
                    saveEdit(textElement, editButton, id, type);
                }
            });
        }
    }

    function saveEdit(textElement, editButton, id, type) {
        const newText = textElement.textContent.trim();
        if (newText !== '') {
            if (type === 'comment') {
                saveEditedComment(id, newText, textElement);
            } else {
                saveEditedQuote(id, newText, textElement);
            }
        }
        textElement.setAttribute('contenteditable', 'false');
        editButton.innerHTML = `
        <svg viewBox="0 0 24 24" width="24" height="24">
            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
        </svg>
    `;

        // 移除 keydown 事件監聽器
        textElement.removeEventListener('keydown', function(event) {
            if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault();
                saveEdit(textElement, editButton, id, type);
            }
        });
    }

    // 編輯評論 支援 container 內編輯
    function saveEditedComment(commentId, newComment, commentTextElement) {
        const token = localStorage.getItem('jwtToken');
        fetch(`/api/book/editComment`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                id: commentId,
                token: token,
                updatedComment: newComment
            })
        })
            .then(response => response.json())
            .then(data => {
                if (data.message) {
                    commentTextElement.textContent = newComment;
                } else {
                    alert("Failed to edit comment.");
                }
            })
            .catch(error => {
                console.error("Error editing comment:", error);
            });
    }

    // 刪除評論
    function deleteComment(commentId, commentDiv) {
        const token = localStorage.getItem('jwtToken');
        fetch('/api/book/deleteComment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                id: commentId,
                token: token
            })
        })
            .then(response => response.json())
            .then(data => {
                // todo 注意回傳 key 是message
                if (data.message) {
                    commentDiv.remove();
                } else {
                    alert("Failed to delete comment.");
                }
            })
            .catch(error => {
                console.error("Error deleting comment:", error);
            });
    }

    // 編輯引言 支援 container 內編輯
    function saveEditedQuote(quoteId, newQuote, quoteTextElement) {
        const token = localStorage.getItem('jwtToken');
        fetch(`/api/book/editQuote`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                id: quoteId,
                token: token,
                updatedQuote: newQuote
            })
        })
            .then(response => response.json())
            .then(data => {
                if (data.message) {
                    quoteTextElement.textContent = newQuote;
                } else {
                    alert("Failed to edit quote.");
                }
            })
            .catch(error => {
                console.error("Error editing quote:", error);
            });
    }

    // 刪除引言
    function deleteQuote(quoteId, quoteDiv) {
        const token = localStorage.getItem('jwtToken');
        fetch('/api/book/deleteQuote', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                id: quoteId,
                token: token
            })
        })
            .then(response => response.json())
            .then(data => {
                // 注意回傳 key 是message
                if (data.message) {
                    quoteDiv.remove(); // 從頁面中刪除該引言
                } else {
                    alert("Failed to delete quote.");
                }
            })
            .catch(error => {
                console.error("Error deleting quote:", error);
            });
    }

    // 渲染評論 支援 container 內編輯
    function renderComments(comments) {
        const commentsContainer = document.getElementById('containerB');
        commentsContainer.innerHTML = "";  // 清空現有評論

        comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.classList.add('comment-container');

            const contentDiv = document.createElement('div');
            contentDiv.classList.add('comment-content');

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;
            commentText.setAttribute('contenteditable', 'false');

            const userNameDiv = document.createElement('div');
            userNameDiv.classList.add('comment-user-name');
            userNameDiv.textContent = comment.userName;

            contentDiv.appendChild(commentText);
            contentDiv.appendChild(userNameDiv);

            commentDiv.appendChild(contentDiv);

            if (userIdFromToken && userIdFromToken === comment.userId) {
                const buttonContainer = document.createElement('div');
                buttonContainer.classList.add('button-container');

                const editButton = document.createElement('button');
                editButton.classList.add('icon-button');
                editButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
            `;
                editButton.addEventListener('click', (e) => {
                    e.stopPropagation();
                    toggleEdit(commentText, editButton, comment.id, 'comment');
                });

                const deleteButton = document.createElement('button');
                deleteButton.classList.add('icon-button');
                deleteButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
            `;
                deleteButton.addEventListener('click', (e) => {
                    e.stopPropagation();
                    deleteComment(comment.id, commentDiv);
                });

                buttonContainer.appendChild(editButton);
                buttonContainer.appendChild(deleteButton);
                commentDiv.appendChild(buttonContainer);
            }

            commentsContainer.appendChild(commentDiv);
        });
    }

    // 選染引言 支援 container 編輯
    function renderQuotes(quotes) {
        const commentsContainer = document.getElementById('containerB');
        commentsContainer.innerHTML = "";  // 清空現有引用

        quotes.forEach(quote => {
            const quoteDiv = document.createElement('div');
            quoteDiv.classList.add('quote-container');

            const contentDiv = document.createElement('div');
            contentDiv.classList.add('quote-content');

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;  // 顯示引用文字
            quoteText.setAttribute('contenteditable', 'false');

            const userNameDiv = document.createElement('div');
            userNameDiv.classList.add('quote-user-name');
            userNameDiv.textContent = quote.userName;  // 顯示 userName

            contentDiv.appendChild(quoteText);
            contentDiv.appendChild(userNameDiv);

            quoteDiv.appendChild(contentDiv);

            // 如果 JWT 中的 userId 與引言的 userId 匹配，顯示編輯和刪除按鈕
            if (userIdFromToken && userIdFromToken === quote.userId) {
                const buttonContainer = document.createElement('div');
                buttonContainer.classList.add('button-container');

                const editButton = document.createElement('button');
                editButton.classList.add('icon-button');
                editButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
            `;
                editButton.addEventListener('click', (e) => {
                    e.stopPropagation();
                    toggleEdit(quoteText, editButton, quote.id, 'quote');
                });

                const deleteButton = document.createElement('button');
                deleteButton.classList.add('icon-button');
                deleteButton.innerHTML = `
                <svg viewBox="0 0 24 24" width="24" height="24">
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
            `;
                deleteButton.addEventListener('click', (e) => {
                    e.stopPropagation();
                    deleteQuote(quote.id, quoteDiv);
                });

                buttonContainer.appendChild(editButton);
                buttonContainer.appendChild(deleteButton);
                quoteDiv.appendChild(buttonContainer);
            }

            commentsContainer.appendChild(quoteDiv);
        });
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
    // const switchCommentButton = document.getElementById('switch-comment');
    // const switchQuoteButton = document.getElementById('switch-quote');
    const switchCommentButton = document.getElementById('b-left');
    const switchQuoteButton = document.getElementById('b-right');
    const submitButton = document.getElementById('submit-button');
    const inputBox = document.getElementById('input-box');
    const commentsContainer = document.getElementById('containerB'); // 這裡應該是評論/引用的容器

    let currentMode = 'comment'; // 預設為 Comment 模式

    // 更新標題和輸入框提示的函數
    function updateUIForMode(mode) {
        if (mode === 'comment') {
            inputBox.placeholder = "Write a Comment";
        } else {
            inputBox.placeholder = "Write a Quote";
        }
    }

    // 初始化UI
    updateUIForMode(currentMode);

    // 切換至 Comment 模式
    switchCommentButton.addEventListener('click', () => {
        currentMode = 'comment';
        updateUIForMode(currentMode);
        loadComments(1); // 切換至評論時重新加載評論
    });

    // 切換至 Quote 模式
    switchQuoteButton.addEventListener('click', () => {
        currentMode = 'quote';
        updateUIForMode(currentMode);
        loadQuotes(1); // 切換至引用時重新加載引用
    });

    // 點擊 或 enter 提交
    async function handleSubmit() {
        const token = localStorage.getItem('jwtToken');
        const commentOrQuote = inputBox.value.trim();
        const urlParams = new URLSearchParams(window.location.search);
        const bookId = urlParams.get('bookId');

        if (!token) {
            alert("Please log in to submit.");
            window.location.href = "account.html";
            return;
        }

        if (commentOrQuote === "") {
            alert("Input cannot be empty.");
            return;
        }

        try {
            let apiUrl;
            let requestBody;

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
                if (currentMode === 'comment') {
                    renderNewComment(commentOrQuote);
                } else {
                    renderNewQuote(commentOrQuote);
                }

                inputBox.value = "";
            } else {
                alert(result.message);
            }
        } catch (error) {
            console.error("Error submitting data:", error);
            alert("Failed to submit.");
        }
    }

// 點擊送出按鈕
    submitButton.addEventListener('click', handleSubmit);

// 監聽輸入框的 keydown 事件
    inputBox.addEventListener('keydown', async (event) => {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault(); // 阻止默認的換行行為
            await handleSubmit();
        }
    });

    // 渲染新評論
    // 只有當前模式為 "comment" 時才渲染新評論
    // todo 目前沒辦法將 userName 不刷新即時顯示，因為 userName 需要從後端拿 Dto
    function renderNewComment(comment) {
        const commentDiv = document.createElement('div');
        commentDiv.classList.add('comment-container');

        const commentText = document.createElement('div');
        commentText.classList.add('comment-text');
        commentText.textContent = comment;  // 顯示新評論文字

        commentDiv.appendChild(commentText);
        commentsContainer.prepend(commentDiv); // 新評論顯示在頂部
    }

    // 渲染新引用
    // 只有當前模式為 "quote" 時才渲染新引用
    // todo 目前沒辦法將 userName 不刷新即時顯示，因為 userName 需要從後端拿 Dto
    function renderNewQuote(quote) {
        const quoteDiv = document.createElement('div');
        quoteDiv.classList.add('quote-container');

        const quoteText = document.createElement('div');
        quoteText.classList.add('quote-text');
        quoteText.textContent = quote;  // 顯示新引用文字

        quoteDiv.appendChild(quoteText);
        commentsContainer.prepend(quoteDiv); // 新引用顯示在頂部
    }
});