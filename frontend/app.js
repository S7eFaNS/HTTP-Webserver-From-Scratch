const goalsContainer =
    document.getElementById("goals");

const form =
    document.getElementById("goal-form");

const findButton =
    document.getElementById("findBtn");

const goalDetails =
    document.getElementById("goal-details");

async function loadGoals() {

    try {

        const response =
            await fetch("/goals");

        const goals =
            await response.json();

        goalsContainer.innerHTML = "";

        goals.forEach(goal => {

            const card =
                document.createElement("div");

            card.classList.add("goal-card");

            card.innerHTML = `
                <h3>${goal.title}</h3>

                <p class="goal-meta">
                    ${goal.dailyTarget}
                </p>

                <p class="goal-meta">
                    ID: ${goal.id}
                </p>

                <p class="goal-meta">
                    Created: ${goal.createdAt}
                </p>

                <button
                    class="delete-btn"
                    onclick="deleteGoal(${goal.id})">
                    Delete
                </button>
            `;

            goalsContainer.appendChild(card);
        });

    } catch (error) {
        console.error(error);
    }
}

async function deleteGoal(id) {

    try {

        const response =
            await fetch(`/goal?id=${id}`, {
                method: "DELETE"
            });

        const message =
            await response.text();

        console.log(message);

        loadGoals();

    } catch (error) {
        console.error(error);
    }
}

async function findGoalById() {

    const id =
        document.getElementById("goalId").value;

    if (!id) {
        return;
    }

    try {

        const response =
            await fetch(`/goal?id=${id}`);

        if (!response.ok) {

            const message =
                await response.text();

            goalDetails.innerHTML = `
                <div class="goal-details-card">
                    ${message}
                </div>
            `;

            return;
        }

        const goal =
            await response.json();

        goalDetails.innerHTML = `
            <div class="goal-details-card">

                <h3>${goal.title}</h3>

                <p>
                    🎯 ${goal.dailyTarget}
                </p>

                <p>
                    ID: ${goal.id}
                </p>

                <p>
                    Created: ${goal.createdAt}
                </p>

            </div>
        `;

    } catch (error) {
        console.error(error);
    }
}

form.addEventListener(
    "submit",
    async (e) => {

        e.preventDefault();

        const title =
            document.getElementById("title")
                    .value
                    .trim();

        const dailyTarget =
            document.getElementById("dailyTarget")
                    .value
                    .trim();

        if (!title || !dailyTarget) {
            return;
        }

        try {

            const response =
                await fetch("/goal", {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/json"
                    },
                    body: JSON.stringify({
                        title,
                        dailyTarget
                    })
                });

            const message =
                await response.text();

            console.log(message);

            form.reset();

            loadGoals();

        } catch (error) {
            console.error(error);
        }
    }
);

findButton.addEventListener(
    "click",
    findGoalById
);

loadGoals();