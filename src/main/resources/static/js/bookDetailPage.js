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

            // // 渲染書籍資訊
            // const bookInfo = `
            //     <div><strong>Book Name:</strong> ${book.bookName}</div>
            //     <div><strong>Author:</strong> ${book.author}</div>
            //     <div><strong>Publisher:</strong> ${book.publisher}</div>
            //     <div><strong>Publish Date:</strong> ${book.publishDate}</div>
            //     <div><strong>ISBN:</strong> ${book.isbn}</div>
            //     <div><strong>Likes:</strong> ${book.like}</div>
            //     <div><strong>Collects:</strong> ${book.collect}</div>
            //     <div><strong>Content:</strong> ${book.content}</div>
            // `;
            // containerA2.innerHTML = bookInfo;

            // 渲染书籍信息
            document.getElementById('bookName').innerHTML = `<span>${book.bookName}</span>`;
            document.getElementById('author').innerHTML = `<span>${book.author}</span>`;
            document.getElementById('publisher').innerHTML = `<strong>出版社：</strong> <span>${book.publisher}</span>`;
            document.getElementById('publishDate').innerHTML = `<strong>出版日期：</strong> <span>${book.publishDate}</span>`;
            document.getElementById('isbn').innerHTML = `<strong>ISBN：</strong> <span>${book.isbn}</span>`;
            document.getElementById('likes').innerHTML = `<strong>點讚數：</strong> <span>${book.like}</span>`;
            document.getElementById('collects').innerHTML = `<strong>收藏數：</strong> <span>${book.collect}</span>`;
            document.getElementById('content').innerHTML = `<span>${book.content}</span>`;

            // function truncateContent(content, limit = 250) {
            //     if (content.length <= limit) return content;
            //     return content.slice(0, limit) + '...';
            // }
            //
            // function renderContent(content) {
            //     const contentElement = document.getElementById('content');
            //     const truncated = truncateContent(content);
            //
            //     if (content.length > 250) {
            //         contentElement.innerHTML = `
            //     <strong>目录：</strong>
            //     <span class="truncated-content">${truncated}</span>
            //     <span class="full-content" style="display:none;">${content}</span>
            //     <button class="expand-button">展开</button>
            // `;
            //
            //         const expandButton = contentElement.querySelector('.expand-button');
            //         expandButton.addEventListener('click', function() {
            //             const truncatedSpan = contentElement.querySelector('.truncated-content');
            //             const fullSpan = contentElement.querySelector('.full-content');
            //
            //             if (truncatedSpan.style.display !== 'none') {
            //                 truncatedSpan.style.display = 'none';
            //                 fullSpan.style.display = 'inline';
            //                 expandButton.textContent = '收起';
            //             } else {
            //                 truncatedSpan.style.display = 'inline';
            //                 fullSpan.style.display = 'none';
            //                 expandButton.textContent = '展开';
            //             }
            //         });
            //     } else {
            //         contentElement.innerHTML = `<strong>目录：</strong> <span>${content}</span>`;
            //     }
            // }
            //
            // renderContent(book.content);

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

    // combine contain b anc contain c
    document.addEventListener("DOMContentLoaded", async function() {
        let commentOffset = 0;
        let quoteOffset = 0;
        const limit = 6;
        let isLoading = false;
        let hasMoreComments = true;
        let hasMoreQuotes = true;
        let currentMode = 'comment'; // 预设为 Comment 模式

        const commentsContainer = document.getElementById('containerB');
        const bLeftButton = document.getElementById('b-left');
        const bRightButton = document.getElementById('b-right');
        const submitButton = document.getElementById('submit-button');
        const inputBox = document.getElementById('input-box');
        let currentLoadFunction = loadComments;

        const urlParams = new URLSearchParams(window.location.search);
        const bookId = urlParams.get('bookId');

        const token = localStorage.getItem('jwtToken');
        let userIdFromToken = null;

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

        // 检查并处理 token
        if (token) {
            if (isTokenExpired(token)) {
                console.error("JWT Token has expired.");
                localStorage.removeItem('jwtToken');
            } else {
                const decodedToken = parseJwt(token);
                userIdFromToken = decodedToken.userId;
            }
        }

        // 加载评论
        async function loadComments() {
            if (isLoading || !hasMoreComments) return;
            isLoading = true;

            try {
                const response = await fetch(`/api/bookPage/switchToComment?bookId=${bookId}&offset=${commentOffset}&limit=${limit}`);
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
                console.error("无法加载评论：", error);
            } finally {
                isLoading = false;
            }
        }

        // 加载引用
        async function loadQuotes() {
            if (isLoading || !hasMoreQuotes) return;
            isLoading = true;

            try {
                const response = await fetch(`/api/bookPage/switchToQuote?bookId=${bookId}&offset=${quoteOffset}&limit=${limit}`);
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
                console.error("无法加载引用：", error);
            } finally {
                isLoading = false;
            }
        }

        // toggleEdit, saveEdit, saveEditedComment, deleteComment, saveEditedQuote, deleteQuote 函数保持不变
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

        // 渲染评论
        function renderComments(comments) {
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

        // 渲染引用
        function renderQuotes(quotes) {
            quotes.forEach(quote => {
                const quoteDiv = document.createElement('div');
                quoteDiv.classList.add('quote-container');

                const contentDiv = document.createElement('div');
                contentDiv.classList.add('quote-content');

                const quoteText = document.createElement('div');
                quoteText.classList.add('quote-text');
                quoteText.textContent = quote.quote;
                quoteText.setAttribute('contenteditable', 'false');

                const userNameDiv = document.createElement('div');
                userNameDiv.classList.add('quote-user-name');
                userNameDiv.textContent = quote.userName;

                contentDiv.appendChild(quoteText);
                contentDiv.appendChild(userNameDiv);

                quoteDiv.appendChild(contentDiv);

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
            const containerBottom = commentsContainer.offsetTop + commentsContainer.offsetHeight;

            if (scrollPosition >= containerBottom - 200 && !isLoading) {
                currentLoadFunction();
            }
        }

        window.addEventListener('scroll', handleScroll);

        function updateUIForMode(mode) {
            if (mode === 'comment') {
                inputBox.placeholder = "Write a Comment";
            } else {
                inputBox.placeholder = "Write a Quote";
            }
        }

        // 初始化UI
        updateUIForMode(currentMode);

        // 切换到评论
        bLeftButton.addEventListener('click', () => {
            currentMode = 'comment';
            updateUIForMode(currentMode);
            commentsContainer.innerHTML = '';
            commentOffset = 0;
            hasMoreComments = true;
            currentLoadFunction = loadComments;
            loadComments();
        });

        // 切换到引用
        bRightButton.addEventListener('click', () => {
            currentMode = 'quote';
            updateUIForMode(currentMode);
            commentsContainer.innerHTML = '';
            quoteOffset = 0;
            hasMoreQuotes = true;
            currentLoadFunction = loadQuotes;
            loadQuotes();
        });

        // 提交评论或引用
        async function handleSubmit() {
            const commentOrQuote = inputBox.value.trim();

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
                    inputBox.value = ""; // 清空输入框

                    // 重新加载评论或引用
                    commentsContainer.innerHTML = ''; // 清空容器
                    if (currentMode === 'comment') {
                        commentOffset = 0;
                        hasMoreComments = true;
                        await loadComments(1);
                    } else {
                        quoteOffset = 0;
                        hasMoreQuotes = true;
                        await loadQuotes(1);
                    }
                    // 滾動到容器頂部以顯示新添加的內容
                    commentsContainer.scrollTop = 0;
                } else {
                    alert(result.message || "Failed to submit.");
                }
            } catch (error) {
                console.error("Error submitting data:", error);
                alert("Failed to submit. Please try again.");
            }
        }

        // 点击提交按钮
        submitButton.addEventListener('click', handleSubmit);

        // 监听输入框的 keydown 事件
        inputBox.addEventListener('keydown', async (event) => {
            if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault(); // 阻止默认的换行行为
                await handleSubmit();
            }
        });

        // 初始加载评论
        loadComments();
});