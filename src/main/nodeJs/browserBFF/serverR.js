import express from 'express';
import cors from 'cors';
const app = express();
const port = 3000;
import productRoutes from  './routes/productRoutes.js';
import categoryRoutes from  './routes/categoryRoutes.js';
import customerRoutes from  './routes/customerRoutes.js';
import purchaseRoutes from  './routes/purchaseRoutes.js';
import attributeRoutes from  './routes/attributeRoutes.js';
import authRoutes from  './routes/authRoutes.js';
// import http from 'http';
// import url from 'url';
// import {response, request} from "express";
// import test from "node:test";
import cookieParser from 'cookie-parser';
app.use(cors({origin: 'http://localhost:5173',credentials: true}));
app.use(express.json());
app.use(cookieParser());

app.use('/product', productRoutes)
app.use('/category', categoryRoutes)
app.use('/customer', customerRoutes)
app.use('/purchase', purchaseRoutes)
app.use('/attribute', attributeRoutes)
app.use('/auth', authRoutes)

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});

