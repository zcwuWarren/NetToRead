// 當頁面載入後呼叫 API 來載入書籍
document.addEventListener("DOMContentLoaded", async function() {
    // 獲取當前頁面的 URL
    const urlParams = new URLSearchParams(window.location.pathname);

    // 假設 URL 格式為 /category/{mainCategory}/{subCategory}
    const pathArray = window.location.pathname.split('/');

    // 獲取 mainCategory 和 subCategory
    const mainCategory = pathArray[2];  // 獲取 mainCategory
    const subCategory = pathArray[3];   // 獲取 subCategory

    const response = await fetch(`/api/bookPage/${mainCategory}/${subCategory}/latest-likes-by-category`);
    const books = await response.json();

    const likesContainer = document.getElementById('latestLikesContainer');
    books.forEach(book => {
        const bookContainer = document.createElement('div');
        bookContainer.classList.add('book-container');

        const bookCover = document.createElement('img');
        bookCover.src = book.bookCover;
        bookCover.classList.add('book-cover');

        const bookName = document.createElement('div');
        bookName.classList.add('book-name');
        bookName.textContent = book.bookName;

        bookContainer.appendChild(bookCover);
        bookContainer.appendChild(bookName);
        likesContainer.appendChild(bookContainer);
    });
});

// document.addEventListener("DOMContentLoaded", async function () {
//     const mainCategory = document.body.getAttribute("data-mainCategory");  // 動態獲取 mainCategory
//     const subCategory = document.body.getAttribute("data-subCategory");    // 動態獲取 subCategory
//
//     try {
//         const response = await fetch(`/api/bookPage/${mainCategory}/${subCategory}/latest-likes-by-category`);
//         const books = await response.json();
//
//         const latestLikesContainer = document.getElementById("latestLikesContainer");
//         books.forEach(book => {
//             const bookDiv = document.createElement("div");
//             bookDiv.classList.add("book-container");
//
//             const bookCover = document.createElement("img");
//             bookCover.src = book.bookCover;
//             bookCover.classList.add("book-cover");
//
//             const bookName = document.createElement("div");
//             bookName.classList.add("book-name");
//             bookName.innerText = book.bookName;
//
//             bookDiv.appendChild(bookCover);
//             bookDiv.appendChild(bookName);
//             latestLikesContainer.appendChild(bookDiv);
//         });
//     } catch (error) {
//         console.error("Error fetching latest likes:", error);
//     }
// });
//
