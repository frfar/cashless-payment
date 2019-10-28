const aesjs = require('aes-js');
const eccrypto = require('eccrypto');
const crypto = require('crypto');
const chalk = require('chalk');
const express = require('express');
const app = express();
let connection = require('./SequelizeConnection');
let sequelize = connection.sequelize();

const cards = sequelize.import('./models/cards');
const transactions = sequelize.import('./models/transactions');



//32 bytes key for aes
var key_256 = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
    29, 30, 31];

let key = new Uint8Array(key_256);

//random 32-byte private key for ecc
const privateKey = eccrypto.generatePrivate();
const publicKey = eccrypto.getPublic(privateKey);

app.get('/', (req, res) => {
    res.send('Welcome to cashless');
})

app.get('/publicKey', (req, res) => {
    console.log(publicKey.toString('base64'));
    res.status(200).send(publicKey.toString('base64'));
})  

//convert key wich is utf8 array to string based 64
const keyBase64 = Buffer.from(String.fromCharCode.apply(null, key)).toString('base64');
//iv based 64 | should random every time?
const iv = crypto.randomBytes(16).toString('base64');

console.log(chalk.yellow('keyBase64 is : ' + keyBase64));
console.log(chalk.yellow('ivBase64 is : ' + iv.toString('base64')));

// cardUniqueId, amount, vendingMachineId, type
//search by uniqueId, if it's credit, add, limit 100.
// if it's debit, check amount limit 0
// remaining amount: encrypt and sign and send back as response
//^ done

// add to transaction table
app.get('/transaction', (req,res) => {
    const amount = req.query.amount;
    const unique_Id = req.query.unique_Id;
    const vendingMachineId = req.query.vendingMachineId;
    const type = req.query.type;

    const STATUS_SUCCESS = 'Success';
    const STATUS_UNDERFLOW = 'Underflow';
    const STATUS_OVERFLOW = 'Overflow';
    const UPPER_LIMIT = 100;
    const LOWER_LIMIT = 0;

    cards.findOne({
        where: {unique_Id : unique_Id}
    }).then((card) => {
        if(card){
            if (type == '1'){ //credit
                if (parseFloat(card.amount) + parseFloat(amount) >= UPPER_LIMIT){ //overflow
                    res.status(400).json({
                        'status': STATUS_OVERFLOW,
                        'amount': card.amount
                    });
                }else{
                    card.update({
                        amount: parseFloat(card.amount) + parseFloat(amount)
                    }).then((updatedCard) => {
                        encryptAndSign(updatedCard.amount).then((msg)=>{
                            //write to table
                            console.log(amount,unique_Id, vendingMachineId, type);
                            addTransaction(amount,unique_Id, vendingMachineId, type);
                            res.status(200).json({
                                'status': STATUS_SUCCESS,
                                'amount': updatedCard.amount,
                                'msg': msg
                            });
                        });
                    });
                }
            }else if(type == '2'){ //debit
                if (parseFloat(card.amount) - parseFloat(amount) < LOWER_LIMIT){ //underflow
                    res.status(400).json({
                        'status': STATUS_UNDERFLOW,
                        'amount': card.amount
                    });
                }else{
                    card.update({
                        amount: parseFloat(card.amount) - parseFloat(amount)
                    }).then((updatedCard) => {
                        encryptAndSign(updatedCard.amount).then((msg)=>{
                            //write to table
                            addTransaction(amount,unique_Id, vendingMachineId, type);
                            res.status(200).json({
                                'status': STATUS_SUCCESS,
                                'amount': updatedCard.amount,
                                'message' : msg
                            });
                        });
                    });
                }
            }else{
                res.status(400).json({'message': 'Invalid type'});
            }
        } else{
            res.status(404).json({'message': 'Card not found'});
        }
    }).catch(err => {
        console.log(err);
        res.status(502).json({'message': 'Internal server error'});
    });
});

const addTransaction = (amount, unique_Id, vendingMachineId, type) => {
    transactions.create({
        card_id : unique_Id,
        vending_machine_id : vendingMachineId,
        amount : amount,
        type : type
    });

    // transactions.build({
    //     card_id : unique_Id,
    //     vending_machine_id : vendingMachineId,
    //     amount : amount,
    //     type : type
    // }).save().then(()=>{
    //     console.log("data created");
    // })
}

const encryptAndSign = (msg) => {
    const encryptedText = encrypt(toString(msg),keyBase64,iv);
    const hashedText = crypto.createHash("sha256").update(encryptedText).digest();
    return eccrypto.sign(privateKey, hashedText).then((sig) => {
        // if (sig) ?
        return {
            'encryptedAmount' : encryptedText.toString('base64'),
            'signature' : sig.toString('base64')
        };
    });
}

const encrypt = (plainText, keyBase64, ivBase64) => {

    const key = Buffer.from(keyBase64, 'base64');
    const iv = Buffer.from(ivBase64, 'base64');

    const cipher = crypto.createCipheriv('aes-256-cbc', key, iv);
    let encrypted = cipher.update(plainText, 'utf8', 'base64')
    encrypted += cipher.final('base64');
    return encrypted;
};

const decrypt = (messagebase64, keyBase64, ivBase64) => {

    const key = Buffer.from(keyBase64, 'base64');
    const iv = Buffer.from(ivBase64, 'base64');

    const decipher = crypto.createDecipheriv('aes-256-cbc', key, iv);
    let decrypted = decipher.update(messagebase64, 'base64');
    decrypted += decipher.final();
    return decrypted;
}

app.listen(3000, (err) => {
    if (err){
        console.log(err);
        throw err;
    }
    console.log('Lisening on port 3000');
})
