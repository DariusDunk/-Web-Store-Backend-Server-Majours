# Garden Webstore - backend
This project serves as a backend server application for a webstore, managing client requests and database calls

---

## Purpose
As demand grows and suppliers struggle to keep pace, the limitations of physical stores become increasingly evident. This project addresses these challenges by introducing an online ordering system that allows customers to conveniently shop from the comfort of their homes, enhancing accessibility.

---

## Table of Contents

1. [Features][features]
2. [Technologies used][TechStacks]
3. [Tests][Test]
4. [Requirements][Requirement]
5. [API overview][endpoints]
6. [Contributors][contributors]



[features]: https://github.com/DariusDunk/Web-Store-Backend-server/blob/master/README.md#features
[TechStacks]: https://github.com/DariusDunk/Web-Store-Backend-server/blob/master/README.md#technologies-used
[Test]: https://github.com/DariusDunk/Web-Store-Backend-server/blob/master/README.md#tests
[Requirement]: https://github.com/DariusDunk/Web-Store-Backend-server/blob/master/README.md#requirements
[endpoints]: https://github.com/DariusDunk/Web-Store-Backend-server/blob/master/README.md#api-overview
[contributors]: https://github.com/DariusDunk/Web-Store-Backend-server/blob/master/README.md#contributors


---

## Features

- A relational database for storing user and store data
- Spring boot and Java based interface
- User accounts and authentication
- CRUD operations for products and clients
- Password hashing
- Methods for searching products:
  - By full name
  - By suggestions
  - By manufacturer
  - By product attributes
- Managing a shopping cart and favourites list
  
---


## Technologies used

- Main Language: `Java`
- Framework: `SpringBoot`
- Password hashing library: `BCrypt`
- Database: `PostgreSQL`
- ORM: `Hibernate` (`JPA`)
- Testing: `JUnit`
  
---

## Tests

- Password hashing
- User login
- User cart check

---

## Requirements

- Java JDK 21
- Gradle: For dependency management
- PostgreSQL: Version 17 or higher

---

## API Overview

---

| Method | Endpoint | Description | 
|--------|----------|-------------|
|`POST`  | `customer/registration`| Register user|
|`POST`| `customer/login`| Login for the user|
|`POST`| `customer/addfavourite`| Add a product to favourites|
|`GET`|`customer/favourites/p/{page}`| Open a page from the favourites list|
|`DELETE`|`customer/removefav`| Remove a product from favourites|
|`POST`|`customer/addtocart`| Adds or removes a product from the user cart|
|`GET`| `customer/cart`| Displays a customer's shopping cart|
|`GET`| `product/search`| Searches products by name|
|`GET`| `product/suggest`| Suggests products based on the string inside the search field|
|`GET`| `product/{productCode}`| Shows detailed information about a product|

---

## Setup and instalation

```
git clone https://github.com/DariusDunk/Web-Store-Backend-server.git
```
