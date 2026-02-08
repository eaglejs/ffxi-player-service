cd app/ &&
npm run build &&
sudo cp -R ~/repos/ffxi-player-service/app/dist/* /var/www/ffxi &&
cd ../

sh ./restart-service.sh
