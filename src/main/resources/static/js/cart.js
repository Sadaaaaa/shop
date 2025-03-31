function updateCartUI(cartSize) {
    const cartSizeElement = document.getElementById("cart-size");
    cartSizeElement.textContent = cartSize > 0 ? cartSize : "0"; // Показываем 0, если корзина пуста
}

// Функция для обновления счетчика на кнопке "Добавить в корзину"
function updateButtonCounter(button, quantity) {
    button.textContent = quantity > 0 ? `Добавить в корзину (${quantity})` : "Добавить в корзину";
}

// Функция для управления видимостью кнопки "Уменьшить"
function toggleDecreaseButtonVisibility(productId, quantity) {
    const form = document.getElementById(`form-${productId}`);
    if (!form) {
        console.error(`Ошибка: форма с id="form-${productId}" не найдена.`);
        return;
    }

    const decreaseButton = form.querySelector(".decrease-button");
    if (decreaseButton) {
        decreaseButton.style.display = quantity > 0 ? "inline-block" : "none";
    }
}

// Функция для обновления счетчика между кнопками "Добавить в корзину" и "-"
function updateQuantityDisplay(productId, quantity) {
    const quantityDisplay = document.getElementById(`quantity-${productId}`);
    if (quantityDisplay) {
        quantityDisplay.textContent = quantity;
    } else {
        console.error(`Ошибка: элемент quantity-${productId} не найден.`);
    }
}

// Инициализация начального состояния кнопок "Уменьшить" и счетчиков при загрузке страницы
document.addEventListener("DOMContentLoaded", function () {
    // Скрываем кнопки "Уменьшить" для товаров с нулевым количеством
    document.querySelectorAll(".add-button").forEach(function (button) {
        const form = button.closest("form");
        const productIdInput = form.querySelector("input[name='productId']");
        if (!productIdInput) return;

        const productId = productIdInput.value;
        const buttonText = button.textContent.trim();
        const quantity = buttonText.includes("(") ? parseInt(buttonText.match(/\((\d+)\)/)[1]) : 0;

        toggleDecreaseButtonVisibility(productId, quantity);
        updateQuantityDisplay(productId, quantity); // Обновляем счетчик
    });

    // Обработчик добавления товара в корзину
    document.querySelectorAll('form[id^="form-"]').forEach(function (form) {
        form.addEventListener("submit", function (event) {
            event.preventDefault();

            const productIdInput = form.querySelector('input[name="productId"]');
            if (!productIdInput) return;

            const productId = productIdInput.value;
            const button = form.querySelector(".add-button");

            fetch("/cart/add", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: `productId=${productId}`,
            })
                .then(response => {
                    if (!response.ok) throw new Error("Ошибка при добавлении товара");
                    return response.json(); // Ожидаем объект Cart
                })
                .then(cart => {
                    updateCartUI(cart.items.length); // Обновляем размер корзины

                    const productItem = cart.items.find(item => item.product.id == productId);
                    const quantity = productItem ? productItem.quantity : 0;

                    // Обновляем счетчик на кнопке "Добавить в корзину"
                    updateButtonCounter(button, quantity);

                    // Обновляем видимость кнопки "Уменьшить"
                    toggleDecreaseButtonVisibility(productId, quantity);

                    // Обновляем счетчик между кнопками
                    updateQuantityDisplay(productId, quantity);
                })
                .catch(error => {
                    console.error("Ошибка:", error);
                });
        });
    });

    // Обработчик уменьшения количества товара
    document.querySelectorAll(".decrease-button").forEach(function (button) {
        button.addEventListener("click", function () {
            const productId = button.getAttribute("data-product-id");
            if (!productId) {
                console.error("Ошибка: productId не найден.");
                return;
            }

            fetch("/cart/decrease", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: `productId=${productId}`,
            })
                .then(response => {
                    if (!response.ok) throw new Error("Ошибка при уменьшении товара");
                    return response.json();
                })
                .then(cart => {
                    updateCartUI(cart.items.length);

                    const form = document.getElementById(`form-${productId}`);
                    if (!form) {
                        console.error(`Ошибка: форма с id="form-${productId}" не найдена.`);
                        return;
                    }

                    const addButton = form.querySelector(".add-button");
                    if (!addButton) {
                        console.error(`Ошибка: кнопка "Добавить в корзину" не найдена в форме form-${productId}`);
                        return;
                    }

                    const productItem = cart.items.find(item => item.product.id == productId);
                    const quantity = productItem ? productItem.quantity : 0;

                    // Обновляем счетчик на кнопке "Добавить в корзину"
                    updateButtonCounter(addButton, quantity);

                    // Обновляем видимость кнопки "Уменьшить"
                    toggleDecreaseButtonVisibility(productId, quantity);

                    // Обновляем счетчик между кнопками
                    updateQuantityDisplay(productId, quantity);
                })
                .catch(error => {
                    console.error("Ошибка:", error);
                });
        });
    });
});