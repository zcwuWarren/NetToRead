// let subcategoryTimeout;
// let isOverSubcategory = false;
//
// document.addEventListener("DOMContentLoaded", async function() {
//     try {
//         const response = await fetch("/api/bookPage/categories");
//         const data = await response.json();
//
//         const categories = {};
//         data.forEach(item => {
//             const { mainCategory, subCategory } = item;
//             if (!categories[mainCategory]) {
//                 categories[mainCategory] = [];
//             }
//             categories[mainCategory].push(subCategory);
//         });
//
//         const mainCategoryContainer = document.getElementById("mainCategoryContainer");
//         const subcategoryContainer = document.getElementById("categorySub");
//
//         // 將每個 mainCategory 添加到主分類欄
//         Object.keys(categories).forEach(mainCategory => {
//             const mainCategoryDiv = document.createElement("div");
//             mainCategoryDiv.classList.add("category-item");
//             mainCategoryDiv.innerText = mainCategory;
//
//             // 當滑鼠移入時顯示對應的次分類
//             mainCategoryDiv.addEventListener("mouseenter", (event) => {
//                 clearTimeout(subcategoryTimeout);
//                 displaySubcategories(categories[mainCategory], event.target, mainCategory);
//             });
//
//             // 當滑鼠移出時延遲隱藏次分類
//             mainCategoryDiv.addEventListener("mouseleave", () => {
//                 subcategoryTimeout = setTimeout(() => {
//                     hideSubcategories();
//                 }, 300);
//             });
//
//             mainCategoryContainer.appendChild(mainCategoryDiv);
//         });
//
//         // 防止次分類立即隱藏
//         subcategoryContainer.addEventListener("mouseenter", () => {
//             clearTimeout(subcategoryTimeout);
//             isOverSubcategory = true;
//         });
//
//         subcategoryContainer.addEventListener("mouseleave", () => {
//             isOverSubcategory = false;
//             hideSubcategories();
//         });
//
//     } catch (error) {
//         console.error("抓取分類資料時發生錯誤：", error);
//     }
// });
//
// function displaySubcategories(subcategories, mainCategoryElement, mainCategory) {
//     const categorySub = document.getElementById("categorySub");
//     categorySub.innerHTML = "";
//
//     subcategories.forEach(subCategory => {
//         const subCategoryDiv = document.createElement("div");
//         subCategoryDiv.classList.add("sub-category-item");
//         subCategoryDiv.innerText = subCategory;
//
//         // 為每個次分類添加點擊事件，綁定至指定的 URL
//         subCategoryDiv.addEventListener("click", () => {
//             const url = `/api/bookPage/${encodeURIComponent(mainCategory)}/${encodeURIComponent(subCategory)}`;
//             // const url = `/category/${encodeURIComponent(mainCategory)}/${encodeURIComponent(subCategory)}`;
//             window.location.href = url;
//         });
//
//         categorySub.appendChild(subCategoryDiv);
//     });
//
//     // 設置次分類的位置
//     const rect = mainCategoryElement.getBoundingClientRect();
//     categorySub.style.top = `${rect.top}px`;
//     categorySub.style.left = `${rect.right}px`;
//     categorySub.style.display = "block";
// }
//
// function hideSubcategories() {
//     const categorySub = document.getElementById("categorySub");
//     categorySub.style.display = "none";
// }

document.addEventListener("DOMContentLoaded", function() {
    // 從 API 獲取分類資料
    fetch("/api/bookPage/categories")
        .then(response => response.json())
        .then(categories => {
            const mainCategoryContainer = document.getElementById('mainCategoryContainer');
            const mainCategories = {};

            // 整理主分類與次分類的資料
            categories.forEach(item => {
                const { mainCategory, subCategory } = item;
                if (!mainCategories[mainCategory]) {
                    mainCategories[mainCategory] = [];
                }
                mainCategories[mainCategory].push(subCategory);
            });

            // 動態生成主分類和次分類
            Object.keys(mainCategories).forEach(mainCategory => {
                const mainCategoryDiv = document.createElement('div');
                mainCategoryDiv.classList.add('category-item');
                mainCategoryDiv.innerText = mainCategory;

                // 生成次分類容器
                const subCategoryContainer = document.createElement('div');
                subCategoryContainer.classList.add('sub-category-container');

                mainCategories[mainCategory].forEach(subCategory => {
                    const subCategoryDiv = document.createElement('div');
                    subCategoryDiv.classList.add('sub-category-item');
                    subCategoryDiv.innerText = subCategory;

                    // 點擊 sub_category 時跳轉到 category.html，並附帶參數
                    subCategoryDiv.addEventListener('click', function() {
                        const url = `category.html?mainCategory=${encodeURIComponent(mainCategory)}&subCategory=${encodeURIComponent(subCategory)}`;
                        window.location.href = url;
                    });

                    subCategoryContainer.appendChild(subCategoryDiv);
                });

                mainCategoryDiv.appendChild(subCategoryContainer);
                mainCategoryContainer.appendChild(mainCategoryDiv);
            });
        })
        .catch(error => console.error("Error fetching categories:", error));
});
