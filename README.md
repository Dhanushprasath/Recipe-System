# Recipe & Nutrition Contribution System

## Overview
The Recipe & Nutrition Contribution System is a full stack web application designed to help users track nutrition, manage recipes, and maintain a healthy lifestyle. Users can contribute recipes, monitor calories, calculate BMI/BMR, and interact with other users through comments and favorites. Administrators have full control over the content and can manage users, recipes, and statistics.

This system uses:

Frontend: HTML, CSS, JavaScript  
Backend: Java (Spring Boot)  
Database: SQL/MySQL  
APIs: USDA Nutrition API for nutrition data  
Local AI Model: Ollama Gemma 3.1B for ingredient and description extraction  

## Features

### User Module
Recipe Management: Add, edit, delete recipes.  
Diet Logging: Log daily meals and track diet.  
Favorites: Mark recipes as favorite for quick access.  
Comments: Comment on other users recipes.  
Nutrition & Calories:  
  Calculate calories, activity level, BMI, BMR, and maintenance calories.  
  Fetch nutritional data automatically using USDA API.  
AI Assistance: Use Ollama Gemma 3.1B (local) to extract ingredients and description for user submitted recipes.  

### Admin Module
Recipe Management: Add, approve, or disapprove recipes.  
Comment Control: Delete any comment made by non admin users.  
User Management: Monitor user activity and statistics.  
Statistics Dashboard: View total users, recipe submissions, approvals, and overall platform usage.  

## Technology Stack
Frontend: HTML, CSS, JavaScript  
Backend: Java with Spring Boot  
Database: SQL/MySQL  
API Integration: USDA Nutrition API  
AI Model: Ollama Gemma 3.1B (local deployment)  
Optional Tools: Lombok, Maven for dependency management  

