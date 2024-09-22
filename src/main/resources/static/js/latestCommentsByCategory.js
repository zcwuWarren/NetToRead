// 當頁面載入後呼叫 API 來載入最新評論
document.addEventListener("DOMContentLoaded", async function() {
    const pathArray = window.location.pathname.split('/');
    const mainCategory = pathArray[2];
    const subCategory = pathArray[3];

    const response = await fetch(`/api/bookPage/${mainCategory}/${subCategory}/latest-comments`);
    const comments = await response.json();

    const commentsContainer = document.getElementById('latestCommentsContainer');
    comments.forEach(comment => {
        const commentContainer = document.createElement('div');
        commentContainer.classList.add('comment-container');

        const commentText = document.createElement('div');
        commentText.classList.add('comment-text');
        commentText.textContent = comment.comment;

        const bookName = document.createElement('div');
        bookName.classList.add('comment-book-name');
        bookName.textContent = comment.bookName;

        commentContainer.appendChild(commentText);
        commentContainer.appendChild(bookName);
        commentsContainer.appendChild(commentContainer);
    });
});

// document.addEventListener("DOMContentLoaded", async function () {
//     const mainCategory = document.body.getAttribute("data-mainCategory");  // 動態獲取 mainCategory
//     const subCategory = document.body.getAttribute("data-subCategory");    // 動態獲取 subCategory
//
//     try {
//         const response = await fetch(`/api/bookPage/${mainCategory}/${subCategory}/latest-comments`);
//         const comments = await response.json();
//
//         const latestCommentsContainer = document.getElementById("latestCommentsContainer");
//         comments.forEach(comment => {
//             const commentDiv = document.createElement("div");
//             commentDiv.classList.add("comment-container");
//
//             const commentText = document.createElement("div");
//             commentText.classList.add("comment-text");
//             commentText.innerText = comment.comment;
//
//             const bookName = document.createElement("div");
//             bookName.classList.add("book-name");
//             bookName.innerText = comment.bookName;
//
//             commentDiv.appendChild(commentText);
//             commentDiv.appendChild(bookName);
//             latestCommentsContainer.appendChild(commentDiv);
//         });
//     } catch (error) {
//         console.error("Error fetching latest comments:", error);
//     }
// });

