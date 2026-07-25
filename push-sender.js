const webpush = require('web-push');
const fs = require('fs');
const path = require('path');

const VAPID_FILE = path.join(__dirname, 'vapid.json');

const cmd = process.argv[2];

if (cmd === 'generate') {
  if (fs.existsSync(VAPID_FILE)) {
    const keys = JSON.parse(fs.readFileSync(VAPID_FILE, 'utf8'));
    console.log(JSON.stringify(keys));
  } else {
    const keys = webpush.generateVAPIDKeys();
    fs.writeFileSync(VAPID_FILE, JSON.stringify(keys, null, 2), 'utf8');
    console.log(JSON.stringify(keys));
  }
  process.exit(0);
}

if (cmd === 'send') {
  const subJson = process.argv[3];
  const payloadJson = process.argv[4];

  if (!fs.existsSync(VAPID_FILE)) {
    console.error("VAPID keys not generated yet!");
    process.exit(1);
  }

  const keys = JSON.parse(fs.readFileSync(VAPID_FILE, 'utf8'));

  webpush.setVapidDetails(
    'mailto:support@foodbridge.com',
    keys.publicKey,
    keys.privateKey
  );

  const subscription = JSON.parse(subJson);
  const payload = payloadJson; // stringified payload

  webpush.sendNotification(subscription, payload)
    .then(result => {
      console.log(JSON.stringify({ success: true, statusCode: result.statusCode }));
      process.exit(0);
    })
    .catch(err => {
      console.error(JSON.stringify({ success: false, error: err.message, statusCode: err.statusCode }));
      process.exit(1);
    });
}
