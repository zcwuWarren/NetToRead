let subcategoryTimeout;
let isOverSubcategory = false;

document.addEventListener("DOMContentLoaded", async function() {
    try {
        const response = await fetch("/api/bookPage/categories");
        const data = await response.json();

        const categories = {};
        data.forEach(item => {
            const { mainCategory, subCategory } = item;
            if (!categories[mainCategory]) {
                categories[mainCategory] = [];
            }
            categories[mainCategory].push(subCategory);
        });

        const mainCategoryContainer = document.getElementById("mainCategoryContainer");
        const subcategoryContainer = document.getElementById("categorySub");

        // 將每個 mainCategory 添加到主分類欄
        Object.keys(categories).forEach(mainCategory => {
            const mainCategoryDiv = document.createElement("div");
            mainCategoryDiv.classList.add("category-item");
            mainCategoryDiv.innerText = mainCategory;

            // 當滑鼠移入時顯示對應的次分類
            mainCategoryDiv.addEventListener("mouseenter", (event) => {
                clearTimeout(subcategoryTimeout);
                displaySubcategories(categories[mainCategory], event.target, mainCategory);
            });

            // 當滑鼠移出時延遲隱藏次分類
            mainCategoryDiv.addEventListener("mouseleave", () => {
                subcategoryTimeout = setTimeout(() => {
                    hideSubcategories();
                }, 300);
            });

            mainCategoryContainer.appendChild(mainCategoryDiv);
        });

        // 防止次分類立即隱藏
        subcategoryContainer.addEventListener("mouseenter", () => {
            clearTimeout(subcategoryTimeout);
            isOverSubcategory = true;
        });

        subcategoryContainer.addEventListener("mouseleave", () => {
            isOverSubcategory = false;
            hideSubcategories();
        });

    } catch (error) {
        console.error("抓取分類資料時發生錯誤：", error);
    }
});

function displaySubcategories(subcategories, mainCategoryElement, mainCategory) {
    const categorySub = document.getElementById("categorySub");
    categorySub.innerHTML = "";

    subcategories.forEach(subCategory => {
        const subCategoryDiv = document.createElement("div");
        subCategoryDiv.classList.add("sub-category-item");
        subCategoryDiv.innerText = subCategory;

        // 為每個次分類添加點擊事件，綁定至指定的 URL
        subCategoryDiv.addEventListener("click", () => {
            const url = `/api/bookPage/${encodeURIComponent(mainCategory)}/${encodeURIComponent(subCategory)}`;
            // const url = `/category/${encodeURIComponent(mainCategory)}/${encodeURIComponent(subCategory)}`;
            window.location.href = url;
        });

        categorySub.appendChild(subCategoryDiv);
    });

    // 設置次分類的位置
    const rect = mainCategoryElement.getBoundingClientRect();
    categorySub.style.top = `${rect.top}px`;
    categorySub.style.left = `${rect.right}px`;
    categorySub.style.display = "block";
}

function hideSubcategories() {
    const categorySub = document.getElementById("categorySub");
    categorySub.style.display = "none";
}
