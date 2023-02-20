#!/bin/bash

cd ui-super-heroes
export NODE_OPTIONS=--openssl-legacy-provider
export API_BASE_URL=http://localhost:8082/
npm run dev &

