#!/bin/bash

DSN=postgresql://localhost/shoppinglist lein ring server-headless 8080
