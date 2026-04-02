const API = "http://localhost:8080";

const isGuest = true; // later: based on login

async function createWorkout() {
    const name = document.getElementById("workoutName").value;

    if (isGuest) {
        // LOCAL STORAGE
        const workouts = JSON.parse(localStorage.getItem("workouts") || "[]");

        const newWorkout = {
            id: Date.now(),
            name,
            date: new Date().toISOString().split("T")[0]
        };

        workouts.push(newWorkout);
        localStorage.setItem("workouts", JSON.stringify(workouts));

        alert("Guest workout created!");
    } else {
        // BACKEND
        const res = await fetch(`${API}/api/workouts`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                name,
                date: new Date().toISOString().split("T")[0]
            })
        });

        const data = await res.json();
        alert("Workout created with ID: " + data.id);
    }
}

async function addSet() {
    const workoutId = document.getElementById("workoutId").value;
    const exerciseTypeId = document.getElementById("exerciseSelect").value;
    const reps = document.getElementById("reps").value;
    const weight = document.getElementById("weight").value;

    await fetch(`${API}/api/sets`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            workoutId,
            exerciseTypeId,
            reps,
            weight
        })
    });

    alert("Set added!");
}

async function loadExercises() {
    const res = await fetch(`${API}/api/exercises`);
    const exercises = await res.json();

    const select = document.getElementById("exerciseSelect");

    exercises.forEach(ex => {
        const option = document.createElement("option");
        option.value = ex.id;
        option.text = ex.name;
        select.appendChild(option);
    });
}

async function loadSets() {
    const workoutId = document.getElementById("viewWorkoutId").value;

    const res = await fetch(`${API}/api/workouts/${workoutId}/sets`);
    const sets = await res.json();

    const list = document.getElementById("setsList");
    list.innerHTML = "";

    sets.forEach(set => {
        const li = document.createElement("li");
        li.innerText = `Exercise ${set.exerciseType.name}: ${set.reps} reps @ ${set.weight}kg`;
        list.appendChild(li);
    });
}

window.onload = loadExercises;