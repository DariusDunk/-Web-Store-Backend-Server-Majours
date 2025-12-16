const express = require('express');
const cors = require('cors');
const app = express();
const port = 3000;
// const Backend_Url = 'http://localhost:1620';
// const Backend_Url = process.env.BACKEND_BASE_URL||'http://localhost:1620';
const productRoutes = require( './routes/productRoutes.js');
const categoryRoutes = require( './routes/categoryRoutes.js');
const customerRoutes = require( './routes/customerRoutes.js');
const purchaseRoutes = require( './routes/purchaseRoutes.js');
const attributeRoutes = require( './routes/attributeRoutes.js');
const refreshRoutes = require( './routes/refreshoRoutes.js');
const http = require('http');
const url = require('url');
const {response, request} = require("express");
const test = require("node:test");
const cookieParser = require('cookie-parser');
app.use(cors({origin: 'http://localhost:5173',credentials: true}));
app.use(express.json());
app.use(cookieParser());

app.use('/product', productRoutes)
app.use('/category', categoryRoutes)
app.use('/customer', customerRoutes)
app.use('/purchase', purchaseRoutes)
app.use('/attribute', attributeRoutes)
app.use('/refresh', refreshRoutes)

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});

