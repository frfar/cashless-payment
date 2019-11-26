const aesjs = require('aes-js');
const eccrypto = require('eccrypto');
const crypto = require('crypto');
const chalk = require('chalk');
const express = require('express');
const app = express();
let connection = require('./SequelizeConnection');
let sequelize = connection.sequelize();

const bodyParser = require('body-parser');
const users = sequelize.import('./models/users');
const cards = sequelize.import('./models/cards');
const transactions = sequelize.import('./models/transactions');
const vending_machines = sequelize.import('./models/vending_machines');
const transaction_types = sequelize.import('./models/transaction_types');
const offline_transaction = sequelize.import('./models/offline_transactions');

//32 bytes key for aes
var key_256 = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
    29, 30, 31];

let key = new Uint8Array(key_256);

//random 32-byte private key for ecc
const privateKey = eccrypto.generatePrivate();
const publicKey = eccrypto.getPublic(privateKey);

let port = require('./configs/port.js.local')
app.set('port', port.number)

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));

app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    res.header("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,OPTIONS");
    if (Object.keys(req.query).length === 0 && req.query.constructor === Object) {
        req.query = req.body;
    }
    next()
})

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

// fetch user by email
app.get('/users', (req,res) => {
    users.findOne({
        where: {email: req.query.email}
        })
        .then(user_info => {
        if (user_info){
            if (user_info.length === 0){
                res.status(200).json({
                    'message': 'No user found'
                });
            }else{
                res.status(200).json({
                    'message': user_info
                });
            }
        }else{
            res.status(404).json({
                'message': 'Internal sever error'
            });
        }
    })
})

// fetch all cards by user id
app.get('/cards', (req,res) => {
    cards.findAll({
        where: {user_id: req.query.user_id}
    }).then(user_cards => {
        if (user_cards){
            if (user_cards.length === 0){
                res.status(200).json({
                    'message': 'No card found'
                });
            }else{
                res.status(200).json({
                    'message': user_cards
                });
            }
        }else{
            res.status(404).json({
                'message': 'Internal sever error'
            });
        }
    })
})

// find all incomplete transactions sorted by timestamps
// will create another to find all incomplete transactions for a given card

// message , code (0,1,2)

const INIT_FIRST_TRANS = 0;
const COMPLETE = 1;
const PREV_INCOMPLETE = 2;
const NO_PREV = 3;
const ERROR = 4;


app.post('/offline_transaction/incomplete', async(req,res) => {
    console.log(req.query);
    const card_id = req.query.card_id;
    const vm_id = req.query.vm_id;
    const remaining_amount = req.query.remaining_amount;
    const timestamp = req.query.timestamp;
    const prev_vm_id = req.query.prev_vm_id;
    const prev_remaining_amount = req.query.prev_remaining_amount;
    const prev_timestamp = req.query.prev_timestamp;
    // caution: amount when query is in format: AB.CD

    await offline_transaction.findAll({
        where : {card_id : card_id}
    }).then((result) => {
        if (result.length === 0){
            addOfflineTransaction(card_id, vm_id, remaining_amount, timestamp, prev_vm_id, prev_remaining_amount, prev_timestamp, 1);
            res.status(200).json({
                'message': 'Initialize first transaction',
                'code' : INIT_FIRST_TRANS
            });
        }else{
            offline_transaction.findOne({
                where: { 
                    card_id :card_id,
                    vm_id : prev_vm_id,
                    remaining_amount : prev_remaining_amount,
                    timestamp : prev_timestamp
                }
            }).then((prev_tran) => {
                if (prev_tran){
                    console.log('prev found');
                    if(prev_tran.complete == '1'){
                        console.log('prev is complete');
                        // if prev_transaction is complete, mark this one complete and add to db
                        // loop and find 
                        addOfflineTransaction(card_id, vm_id, remaining_amount, timestamp, prev_vm_id, prev_remaining_amount, prev_timestamp, 1).then(transaction => {
                            offline_transaction.findAll({
                                where: { 
                                    card_id: card_id,
                                    complete: 0
                                },
                                order: [['timestamp', 'ASC']]
                            }).then(async(transactions) => {
                                if (transactions){ // if transactions.length == 0, means no previous incompete for the input transaction, it still goes here
                                    console.log(transactions.length);
                                    let cur = {vm_id, remaining_amount, timestamp};
                                    for (let i = 0; i < transactions.length; i++) {
                                        if (transactions[i].prev_vm_id == cur.vm_id && transactions[i].prev_remaining_amount == cur.remaining_amount && transactions[i].prev_timestamp == cur.timestamp){
                                            console.log('here');
                                            await offline_transaction.findOne({
                                                where:{
                                                    id : transactions[i].id
                                                }
                                            }).then(t => {
                                                t.update({
                                                    complete: 1
                                                }).then(updated_transaction => {
                                                    console.log('transaction ' + updated_transaction.id + ' updated')
                                                })
                                            }); 
                                            cur.vm_id = transactions[i].vm_id;
                                            cur.remaining_amount = transactions[i].remaining_amount;
                                            cur.timestamp = transactions[i].timestamp;
                                        }else
                                            break;
                                    }
                                    res.status(200).json({
                                        'message': 'transaction is complete',
                                        'code' : COMPLETE
                                    });
        
                                }else{
                                    res.status(200).json({
                                        'message': 'Error',
                                        'error' : ERROR
                                    });
                                }
                            });
        
                        })
                    }else{ // else incomplete, so add the current one to db with incomplete state
                        addOfflineTransaction(card_id, vm_id, remaining_amount, timestamp, prev_vm_id, prev_remaining_amount, prev_timestamp, 0);
                    res.status(200).json({
                        'message': 'Previous PR incomplete => Transaction incomplete, added to db',
                        'code' : PREV_INCOMPLETE
                    });
                    }
                }else{ // not found prev tran, so add so add the current one to db with incomplete state
                    addOfflineTransaction(card_id, vm_id, remaining_amount, timestamp, prev_vm_id, prev_remaining_amount, prev_timestamp, 0);
                    res.status(200).json({
                        'message': 'No previous transaction found => Transaction incomplete, added to db',
                        'code' : NO_PREV
                    });
                }
            });
        }
    })

    
});

const addOfflineTransaction = (card_id, vm_id, remaining_amount, timestamp, prev_vm_id, prev_remaining_amount, prev_timestamp,complete) => {
    return new Promise((resolve, reject) => {
        offline_transaction.create({
            card_id : card_id,
            vm_id : vm_id,
            remaining_amount : remaining_amount,
            timestamp : timestamp,
            prev_vm_id : prev_vm_id,
            prev_remaining_amount : prev_remaining_amount,
            prev_timestamp : prev_timestamp,
            complete : complete
        }).then((transaction)=>{
            if(transaction) 
                resolve(transaction);
            else{
                reject('error');
            }
        });
    })
    
}



// find all completed transactions for a card
app.get('/offline_transaction/complete', (req,res) => {
    const card_id = req.query.card_id;
    offline_transaction.findAll({
        where: {
            card_id : card_id,
            complete : '1'
        },
        order: [
            ['timestamp', 'DESC']
        ],
        offset: parseInt(req.query.offset) || 0,
        limit: parseInt(req.query.limit) || null
    }).then((transactions) => {
        if (transactions){
            if (transactions.length === 0){
                res.status(200).json({
                    'message': 'No complete transaction found for card'
                });
            }else{
                res.status(200).json({
                    'message': transactions
                });
            }
        }else{
            res.status(404).json({
                'message': 'Internal sever error'
            });
        }
    });
});



// cardUniqueId, amount, vendingMachineId, type
//search by uniqueId, if it's credit, add, limit 100.
// if it's debit, check amount limit 0
// remaining amount: encrypt and sign and send back as response
//^ done

// add to transaction table
//^done

// change stuff
// return ecrypted remaining amount, not amount of transaction.
//^done
app.get('/transaction', (req,res) => {
    const amount = req.query.amount;
    const unique_Id = req.query.unique_Id;
    const vendingMachineName = req.query.vendingMachineName;
    const type = req.query.type;
    const passcode = req.query.passcode;

    const STATUS_SUCCESS = 'Success';
    const STATUS_FAIL = 'Fail';
    const MSG_UNDERFLOW = 'Underflow';
    const MSG_OVERFLOW = 'Overflow';
    const UPPER_LIMIT = 100;
    const LOWER_LIMIT = 0;

    cards.findOne({
        where: {unique_Id : unique_Id}
    }).then((card) => {
        if(card){
            if (card.passcode == passcode){
                vending_machines.findOne({
                    where: {name: vendingMachineName}
                }).then((vm) => {
                    if(vm){
                        transaction_types.findOne({
                            where: {type: type}
                        }).then((type) => {
                            if(type.id == '1'){ //credit
                                if(parseFloat(card.amount) + parseFloat(amount) >= UPPER_LIMIT){ //overflow
                                    res.status(200).json({
                                        'status': STATUS_FAIL,
                                        'amount': card.amount,
                                        'message': MSG_OVERFLOW
                                    });
                                }else{
                                    card.update({
                                        amount: Math.round(parseFloat(card.amount) * 100) / 100  +  Math.round(parseFloat(amount) * 100) / 100
                                    }).then((updatedCard) => {
                                        encryptAndSign(updatedCard.amount).then((msg)=>{
                                            //write to transaction table
                                            addTransaction(amount,card.id, vm.id, type.id);
                                            res.status(200).json({
                                                'status': STATUS_SUCCESS,
                                                'amount': Math.round(parseFloat(updatedCard.amount) * 100) / 100,
                                                'message': msg
                                            });
                                        });
                                    });
                                }
                            }else if(type.id == '2'){ //debit
                                if (parseFloat(card.amount) - parseFloat(amount) < LOWER_LIMIT){ //underflow
                                    res.status(200).json({
                                        'status': STATUS_FAIL,
                                        'amount': card.amount,
                                        'message' : MSG_UNDERFLOW
                                    });
                                }else{
                                    card.update({
                                        amount: Math.round(parseFloat(card.amount) * 100) / 100  -  Math.round(parseFloat(amount) * 100) / 100
                                    }).then((updatedCard) => {
                                        encryptAndSign(updatedCard.amount).then((msg)=>{
                                            //write to transaction table
                                            addTransaction(amount,card.id, vm.id, type.id);
                                            res.status(200).json({
                                                'status': STATUS_SUCCESS,
                                                'amount': Math.round(parseFloat(updatedCard.amount) * 100) / 100,
                                                'message' : msg
                                            });
                                        });
                                    });
                                }
                            }else{
                                res.status(200).json({
                                    'status': STATUS_FAIL,
                                    'message': 'Internal server error'
                                });
                            }
                        }).catch(err => {
                            res.status(200).json({
                                'status' : STATUS_FAIL,
                                'message': 'Invalid type'
                            });
                        })
                    }else{
                        res.status(200).json({
                            'status' : STATUS_FAIL,
                            'message': 'Invalid vending machine'
                        });
                    }
                })
            }else{ //invalid passcode
                res.status(200).json({
                    'status' : STATUS_FAIL,
                    'message': 'Wrong passcode'
                });
            }
        } else{ // invalid cardId
            res.status(200).json({
                'status' : STATUS_FAIL,
                'message': 'Card not found'
            });
        }
    }).catch(err => {
        console.log(err);
        res.status(502).json({
            'status' : STATUS_FAIL,
            'message': 'Internal server error'
        });
    });
});


const addTransaction = (amount, cardId, vendingMachineId, type) => {
    transactions.create({
        card_id : cardId,
        vending_machine_id : vendingMachineId,
        amount : amount,
        type : type
    }).catch(err => {
        console.log(err);
    })
}

const encryptAndSign = (msg) => {
    const encryptedText = encrypt(toString(msg),keyBase64,iv);
    const hashedText = crypto.createHash("sha256").update(encryptedText).digest();
    return eccrypto.sign(privateKey, hashedText).then((sig) => {
        // if sig not ok?
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

app.listen(app.get('port'), (err) => {
    if (err){
        console.log(err);
        throw err;
    }
    console.log('Lisening on port ' + app.get('port'));
})
