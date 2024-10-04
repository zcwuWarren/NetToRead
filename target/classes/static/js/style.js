// // Simulate categories and subcategories
// const subCategories = {
//     'Category 1': ['Sub 1-1', 'Sub 1-2', 'Sub 1-3'],
//     'Category 2': ['Sub 2-1', 'Sub 2-2', 'Sub 2-3'],
//     'Category 3': ['Sub 3-1', 'Sub 3-2', 'Sub 3-3'],
// };
//
// document.querySelectorAll('.category-item').forEach(item => {
//     item.addEventListener('mouseover', function() {
//         const categoryName = this.getAttribute('data-category');
//         const subCategoryBox = document.getElementById('categorySub');
//         subCategoryBox.innerHTML = ''; // Clear previous subcategories
//         subCategories[categoryName].forEach(sub => {
//             const div = document.createElement('div');
//             div.classList.add('category-item');
//             div.innerText = sub;
//             subCategoryBox.appendChild(div);
//         });
//         subCategoryBox.style.display = 'block'; // Show subcategory box
//     });
// });
//
// document.getElementById('categoryDropdown').addEventListener('mouseleave', function() {
//     document.getElementById('categorySub').style.display = 'none'; // Hide subcategories when mouse leaves
// });
//
// document.getElementById('profileBtn').addEventListener('click', function() {
//     // Here you can redirect to the profile page if necessary.
// });

// 動態生成 Category 和 Profile 的邏輯

document.addEventListener("DOMContentLoaded", function() {
    // 動態加載分類
    fetch("/api/bookPage/categories")
        .then(response => response.json())
        .then(categories => {
            const categoryDropdown = document.getElementById('categoryDropdown');
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

                const subCategoryContainer = document.createElement('div');
                mainCategories[mainCategory].forEach(subCategory => {
                    const subCategoryDiv = document.createElement('div');
                    subCategoryDiv.classList.add('sub-category-item');
                    subCategoryDiv.innerText = subCategory;
                    subCategoryContainer.appendChild(subCategoryDiv);
                });

                mainCategoryDiv.appendChild(subCategoryContainer);
                categoryDropdown.appendChild(mainCategoryDiv);
            });
        })
        .catch(error => console.error("Error fetching categories:", error));

    // Profile Dropdown
    document.getElementById('header-3').addEventListener('mouseover', function() {
        document.getElementById('profileDropdown').style.display = 'block';
    });

    document.getElementById('header-3').addEventListener('mouseleave', function() {
        document.getElementById('profileDropdown').style.display = 'none';
    });
});

// switch likes, collects Button
document.addEventListener('DOMContentLoaded', function() {
    const switchLikes = document.getElementById('switch-likes');
    const switchCollects = document.getElementById('switch-collects');

    // 設置初始狀態
    switchLikes.classList.add('active');

    // 切換到 Likes
    switchLikes.addEventListener('click', function() {
        this.classList.add('active');
        switchCollects.classList.remove('active');
    });

    // 切換到 Collects
    switchCollects.addEventListener('click', function() {
        this.classList.add('active');
        switchLikes.classList.remove('active');
    });
});

// switch comments, quotes Button
document.addEventListener('DOMContentLoaded', function() {
    const switchComment = document.getElementById('b-left');
    const switchQuotes = document.getElementById('b-right');

    // 設置初始狀態
    switchComment.classList.add('active');

    // 切換到 Likes
    switchComment.addEventListener('click', function() {
        this.classList.add('active');
        switchQuotes.classList.remove('active');
    });

    // 切換到 Collects
    switchQuotes.addEventListener('click', function() {
        this.classList.add('active');
        switchComment.classList.remove('active');
    });
});
