const button = document.getElementById("btn");
const message = document.getElementById("message");

button.addEventListener("click", () => {
    message.textContent =
        "JavaScript loaded from your custom HTTP server!";
});