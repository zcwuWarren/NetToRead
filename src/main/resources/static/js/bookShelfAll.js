document.addEventListener("DOMContentLoaded", async function() {
    try {
        const response = await fetch("/api/bookPage/latest-likes");
        const books = await response.json();

        const latestLikesContainer = document.getElementById("latestLikesContainer");

        books.forEach(book => {
            const bookContainer = document.createElement("div");
            bookContainer.classList.add("book-container");

            const bookCover = document.createElement("img");
            bookCover.classList.add("book-cover");
            bookCover.src = book.bookCover; // 圖片 URL

            const bookName = document.createElement("div");
            bookName.classList.add("book-name");
            bookName.innerText = book.bookName; // 書籍名稱

            // 綁定 bookId 並設置點擊事件
            bookContainer.addEventListener("click", () => {
                const url = `/bookDetail.html?bookId=${book.bookId}`; // 使用書籍的 bookId 作為查詢參數
                window.location.href = url; // 跳轉至指定的頁面
            });

            bookContainer.appendChild(bookCover);
            bookContainer.appendChild(bookName);

            latestLikesContainer.appendChild(bookContainer);
        });
    } catch (error) {
        console.error("無法載入書籍資料：", error);
    }
});
