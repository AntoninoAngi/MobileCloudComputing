# Group 1 Project - README

## Overview
This application enables a user, previously registered through the *sign up* feature, to access to his projects, create new ones - individual or group projects, by adding other users - and then insert tasks, pictures and files inside each project.
## How it works
When **signing up** the user inserts a Display Name (unique), an email address, a password and an optional profile picture (taken both by camera or gallery). If the Display Name has already been chosen, the application suggests other 3 alternatives.
After **logging up** with the inserted email address and password, the user can create **new projects** by inserting a name, a description, a deadline and some keywords (3 at most) and an optional project icon.
To each project, pictures, files and one or more **tasks** can be associated, by inserting a description, a deadline and assigned them to one or more users.
* If no user is assigned to a task, it has a *pending* status
* When a user is assigned to a task, the status changes to *on-going*
* When the task is completed, the status changes to *completed*

The user can insert the description for a task in multiple ways:
* Just typing
* Uploading an image from the phone's gallery, the description is automatically retrieved by the text on the image
* Registering a voice note, the description is automatically retrieved

If the task's description is written in a foreign language, the user, by clicking a button can translate it to his language.
Users involved in a group project are also able to generate a report of the project. The outcome is a PDF file which contains: the project name, the list of members, and all related task events.

### Components
The application consists of two components: a **frontend** and a **backend**.
The frontend is the user interface running on a mobile device and has been implemented as an **Android** application.
The backend consists on the database and the logic for processing and storage of  and it relies on services in the [Google Cloud Platform](https://cloud.google.com/products/), particularly, [Firebase](https://firebase.google.com/) and the [Cloud Storage](https://firebase.google.com/docs/storage/)


## Installation - Build and Deploy

From the main directory use following commands from the terminal:
1. `$cd Scripts`
2. `$chmod +x deploy.sh`
3. `$./deploy.sh`

When the deployment is completed, the apk file (*apk-debug.apk*) is found into the *OUTPUT_DIR* folder which is located into the parent one.

## Contact
If you have problems, questions, ideas or suggestions, please contact us by sending an email.

| Partecipant | Contact |
|--|--|
| Angi Antonino | antoninoangi96@gmail.com |
| Ramos Alberto | alberto.fariacoutinhodasilveiraramos@aalto.fi |
| Pace Daniele | daniele.pace@aalto.fi |
| Müller Moritz | moritz.mueller1707@gmail.com |
| Bellami Antoine | antoinemichel.bellami@aalto.fi |



