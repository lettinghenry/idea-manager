# Task Management App

This is a simple Android application for managing a list of tasks. Users can view their tasks, mark them as complete or incomplete using checkboxes, and add new tasks.

## Features

*   **Task List Display:** The app displays a list of tasks using a `RecyclerView`. Each task shows its title/description and a checkbox indicating whether it's completed.
*   **Task Completion:** Users can tap the checkbox next to a task to toggle its completion status.  The app persists these changes in a database.
*   **Toolbar:** The app includes a toolbar at the top. It contains a filtering mechanism that filters between ALL, PENDING and COMPLETE tasks.
*   **Floating Action Button (FAB):** A FAB is present, for adding new tasks.

## Architecture

The app follows a basic MVVM (Model-View-ViewModel) architecture, promoting separation of concerns:

*   **Model:** (Represented by the `Task` data class and the underlying database) This layer represents the data. It includes:
    *   A `Task` data class ( database entity) that stores information about each task (ID, title/description, completion status).
    *   A database ( Room) for persistent storage of tasks.
    *   A `TaskRepository` class that acts as an abstraction layer between the ViewModel and the database.

*   **View:** (Represented by  the `CreateTaskActivity`, `ViewEditTaskActivity`,`MainActivity`, layouts (`activity_main.xml`, `item_task.xml`), and `TaskAdapter`)  This layer handles the user interface and displays data to the user. It includes:
    *   `MainActivity`: The main activity that hosts the task list. It initializes the UI, gets data from the ViewModel, and observes changes to update the display.  It sets up the `RecyclerView`, `TaskAdapter`, toolbar, and FAB (though FAB action is not yet implemented).  It obtains the `TaskViewModel` using the `viewModels()` property delegate, simplifying ViewModel initialization.
    *   `activity_main.xml`: The layout file for the main activity, containing the `RecyclerView`, toolbar, and FAB.
    *   `item_task.xml`: The layout file for each task item in the `RecyclerView`, containing a `TextView` for the task title and a `CheckBox` for completion status.
    *   `TaskAdapter`:  A `RecyclerView.Adapter` that binds task data to the views in the `RecyclerView`.  It receives a list of tasks and a callback function (`onTaskCheckedChange`) from the Activity.  When a checkbox state changes, the adapter invokes the callback to notify the Activity.

*   **ViewModel:** (Represented by the `TaskViewModel` class)  This layer acts as a mediator between the View and the Model. It holds UI-related data and handles user interactions.  It includes:
    *   `TaskViewModel`:  Holds the list of tasks as a `LiveData`.  It retrieves the initial task list from the `TaskRepository` upon creation to handle changes in task completion status.  This function updates the task in the `TaskRepository`  and then refreshes the task list.

## Data Flow

1.  When `MainActivity` is created:
    *   It obtains an instance of `TaskViewModel` using the `by viewModels()` property delegate: `val taskViewModel: TaskViewModel by viewModels()`.  This handles ViewModel creation and lifecycle management automatically.
    *   It initializes the `RecyclerView` with a `TaskAdapter` (initially with an empty list). The adapter receives a callback function, `onTaskCheckedChange`, defined in the Activity.
    *   It observes the `tasks` `LiveData` from the `TaskViewModel`.
2.  When `TaskViewModel` is created (which is handled automatically by the `viewModels()` delegate):
    *   It retrieves the initial list of tasks.
    *   It posts the task list to its `tasks` `LiveData`, making it available to observers.
3.  When the `tasks` `LiveData` in `TaskViewModel` changes (initially, and whenever the list is updated):
    *   The observer in `MainActivity`'s `onCreate` method is triggered.
    *   The observer calls `taskAdapter.updateTasks(tasks)`, passing the new task list to the adapter.
    *   The adapter updates its internal list and calls `notifyDataSetChanged()` to refresh the `RecyclerView`.
4.  When the user taps a checkbox in the `RecyclerView`:
    *   The `OnCheckedChangeListener` in the corresponding `TaskViewHolder` is triggered.
    *   The listener calls the `onTaskCheckedChange` callback function (which is passed from `MainActivity` to the adapter).
    *   `MainActivity`'s `onTaskCheckedChange` function is executed.  It receives the `Task` object and the new checked state.
    *   `MainActivity` calls `taskViewModel.updateTaskCompletion(task, isChecked)`, passing the task and new completion status.
5.  In `TaskViewModel`:
    *   `updateTaskCompletion` updates the task's `isCompleted` property.
    *   It updates the task data This  involves performing the update on a background thread using `viewModelScope.launch(Dispatchers.IO)`.
    *   It then refreshes the task list, ensuring the UI reflects the latest data. This updated list then propagates through the `LiveData` to the `MainActivity` and `RecyclerView` as described above.

## Future Improvements

*   **Implement Search:** Connect the search icon in the toolbar to a search feature.  This might involve filtering the task list based on user input.
*   **Error Handling:** Add error handling (e.g., try-catch blocks, displaying error messages to the user) for potential issues like data access failures.
*   **Notifications:** Implement notifications for taks that are almost due.
*   **UI Enhancements:**
*   **Architecture IMprovement:** Migrate from XML-based layout to Jetpack Compose.
*   **Empty state handling** displaying a message when there are no tasks.
*   **Testing:** Write unit or integration tests to ensure the app's functionality is correct and robust.

## Setup and Running

1.  **Clone the repository.**
2.  **Open the project** in Android Studio.
3.  **Ensure you have the necessary dependencies**, including the appropriate `androidx.activity:activity-ktx` dependency for the `by viewModels()` delegate.  You'll also likely need the `androidx.lifecycle:lifecycle-viewmodel-ktx` dependency.  Check your `build.gradle` file.
4.  **Build and run** the app on an Android emulator or a physical device.  (Run > Run 'app')


I created this guide on https://markdownlivepreview.com/
