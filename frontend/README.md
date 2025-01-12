# Todo Management Frontend

A modern web application for organizing Todos and managing team assignments. Built with Vue.js and TypeScript <http://localhost:5173>.

## Features
- Todo management (create, update, delete)
- Team member (Assignee) management
- Status tracking and activity feed
- Weekly due date overview
- Export functionality (CSV)
- Toast notifications 

## Prerequisites

Install [Node.js](https://nodejs.org/en/) and ensure that the root folder of its installation is added to your PATH. 
You can check with this command: `echo %PATH%` (or echo $PATH on Linux / Git Bash).

## Tech Stack
- Vue.js 3
- TypeScript
- Vite
- Vue Router
- FontAwesome Icons
- Agnostic UI (Toast notifications)

## Development Setup

### Recommended IDE Setup
There are multiple possibilities:
- [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin)
- [WebStorm](https://www.jetbrains.com/webstorm/) (brings the required plugins natively)
- Your favorite text editor + CLI

## Project Setup

Install the required dependencies with

```sh
npm install
```

Then you are good to go with

```sh
npm run dev
```

Then, the site is available at <http://localhost:5173/>.

To deploy your code to production, you can compile and minify using

```sh
npm run build
```

Also, there is linter available:

```sh
npm run lint
```
