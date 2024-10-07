/* reviewsByUserId.js */
document.addEventListener("DOMContentLoaded", async function() {
    let commentOffset = 0;
    let quoteOffset = 0;
    const limit = 50;
    let isLoading = false;
    let hasMoreComments = true;
    let hasMoreQuotes = true;

    const containerB = document.getElementById('containerB');
    const bLeftButton = document.getElementById('b-left');
    const bRightButton = document.getElementById('b-right');
    const loadingContainer = document.querySelector('.loading-container-b');
    const bookshelfReviewTitleCommentQuote = document.getElementById('bookshelfReviewTitleCommentQuote').querySelector('h2');


    let currentLoadFunction = loadComments;

    // 提取 localStorage 中的 jwtToken
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        console.error("No JWT token found in localStorage");
        return;
    }

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

    // 設置初始 active 狀態
    setActiveState(bLeftButton);
    updateBookshelfTitle('comments');

    // 顯示載入動畫
    function showLoading() {
        loadingContainer.style.display = 'flex';
        containerB.classList.remove('loaded');
    }

    // 隱藏載入動畫
    function hideLoading() {
        loadingContainer.style.display = 'none';
        containerB.classList.add('loaded');
    }

    async function loadComments() {
        if (isLoading || !hasMoreComments) return;
        isLoading = true;
        showLoading();

        try {
            const response = await fetch(`/api/userPage/myComment?offset=${commentOffset}&limit=${limit}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ token: token })
            });
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
            hideLoading();
        }
    }

    async function loadQuotes() {
        if (isLoading || !hasMoreQuotes) return;
        isLoading = true;
        showLoading();

        try {
            const response = await fetch(`/api/userPage/myQuote?offset=${quoteOffset}&limit=${limit}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ token: token })
            });
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
            hideLoading();
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

            commentDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${comment.bookId}`;
            });

            const contentDiv = document.createElement('div');
            contentDiv.classList.add('comment-content');

            const commentText = document.createElement('div');
            commentText.classList.add('comment-text');
            commentText.textContent = comment.comment;  // 顯示評論文字
            commentText.setAttribute('contenteditable', 'false');

            const bookName = document.createElement('div');
            bookName.classList.add('comment-book-name');
            bookName.textContent = comment.bookName;  // 顯示 userName

            contentDiv.appendChild(commentText);
            contentDiv.appendChild(bookName);

            commentDiv.appendChild(contentDiv);

            // 如果 JWT 中的 userId 與評論的 userId 匹配，顯示編輯和刪除按鈕
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

            quoteDiv.addEventListener('click', function() {
                window.location.href = `/bookDetail.html?bookId=${quote.bookId}`;
            });

            const contentDiv = document.createElement('div');
            contentDiv.classList.add('quote-content');

            const quoteText = document.createElement('div');
            quoteText.classList.add('quote-text');
            quoteText.textContent = quote.quote;  // 顯示引用文字
            quoteText.setAttribute('contenteditable', 'false');

            const bookName = document.createElement('div');
            bookName.classList.add('quote-book-name');
            bookName.textContent = quote.bookName;  // 顯示 userName

            contentDiv.appendChild(quoteText);
            contentDiv.appendChild(bookName);

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
        setActiveState(bLeftButton);
        removeActiveState(bRightButton);
        updateBookshelfTitle('comments');
        loadComments();
    });

    // 切換到引用
    bRightButton.addEventListener('click', () => {
        containerB.innerHTML = '';
        quoteOffset = 0;
        hasMoreQuotes = true;
        currentLoadFunction = loadQuotes;
        setActiveState(bRightButton);
        removeActiveState(bLeftButton);
        updateBookshelfTitle('quotes');
        loadQuotes();
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
        if (type === 'comments') {
            bookshelfReviewTitleCommentQuote.textContent = '我的評論';
        } else if (type === 'quotes') {
            bookshelfReviewTitleCommentQuote.textContent = '我的引言';
        }
    }
});