To run the UI, we need to export 2 env variables:
to build the application
export NODE_OPTIONS=--openssl-legacy-provider


To pass the api url 
export API_BASE_URL=http://localhost:8082/


To run the application run: 
npm run dev

I created start.sh, just as guideline about all the things we need to start to have the application running