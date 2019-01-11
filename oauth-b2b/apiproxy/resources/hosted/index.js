const express = require('express')
const bodyParser = require('body-parser');
const {GoogleToken} = require('gtoken');

const app = express()
app.use(bodyParser.json()); // for parsing application/json


function getToken(serviceAccountEmail, privateKey) {
    const gtoken = new GoogleToken({
        email: serviceAccountEmail,
        scope: ['https://www.googleapis.com/auth/cloud-platform'],
        key: privateKey
    });

    return new Promise((resolve, reject) => {
        gtoken.getToken(function(err, token) {
            if (err) {
                reject(err);
            }
            // console.log(`Token is: ${token}`);
            resolve(token);
        });
    })
}


app.post('/accesstoken', function (req, res) {
    const grantType = req.body.grantType;
    const scopes = req.body.scopes;
    const key = req.body.key;

    getToken(key.client_email, key.private_key )
    .then(token => {
        res.status(200).send(token)
    })
    .catch(err => {
        res.status(500).send({ error: JSON.stringify(err) });
    })
//   res.json({ hello: "Hello accesstoken! " + JSON.stringify(req.body) })
})


var server = app.listen(process.env.PORT || 9000, function () {
    console.log('Listening on port %d', server.address().port)
})
