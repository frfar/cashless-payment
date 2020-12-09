import React, {useEffect, useState} from "react";
import "./Home.css";
import {Link} from "react-router-dom";
import {Button, FormGroup, FormControl, FormLabel} from "react-bootstrap";
import {useHistory} from "react-router-dom";

import axios from "axios";
import styled from "styled-components";
import ReactTable from "./ReactTable";


const Styles = styled.div`
  padding: 1rem;

  table {
    border-spacing: 0;
    border: 1px solid black;

    tr {
      :last-child {
        td {
          border-bottom: 0;
        }
      }
    }

    th,
    td {
      margin: 0;
      padding: 0.5rem;
      border-bottom: 1px solid black;
      border-right: 1px solid black;

      :last-child {
        border-right: 0;
      }
    }
  }

  .pagination {
    padding: 0.5rem;
  }
`;


function NumberedList(props) {

    const history = useHistory();

    const vendors = props.vendors;
    const listItems = vendors.map((vendor) =>
        <li key={vendor.id}>
            <Link to={{
                pathname: "vendor_transaction",
                vendor: vendor,
                isAdmin: props.isAdmin
            }}>
            {vendor.name} ({vendor.email})
            </Link>
        </li>
    );
    return (
        <ul>{listItems}</ul>
    );
}

export default function Home(props) {
    const [vendors, setVendors] = useState([]);
    const [transactions, setTransactions] = useState([]);
    const [amount, setAmount] = useState("");
    const [vendID, setVendID] = useState("");
    function validateForm() {
        return amount.length > 0 && vendID.length > 0;
    }

    function handleSubmit(event) {
        console.log("here");
        event.preventDefault();
        const postData = {
            amount: amount,
            vendID: vendID
        }

        axios.post(`http://localhost:3000/noRaspTransaction`, postData)
            .then(
                res => {
                    const response = res.data;
                    const token = res.headers["auth-token"];
                    if (token !== undefined) {
                        props.handleTransaction(amount, vendID);
                        // eslint-disable-next-line no-restricted-globals
                        history.push("/")
                    } else {
                        alert("transaction failed");
                    }
                }
            ).catch(
            err => {
                if (err.response !== undefined && err.response.data.message !== undefined) {
                    alert(err.response.data.message);
                }
                console.log(err.response);
            }
        )


    }



    useEffect(() => {
        props.isAdmin && axios.get("http://localhost:3000/vendors?has_transactions=true").then(
            result => {
                setVendors(result.data);
            }
        );
        props.isAdmin && axios.get(`http://localhost:3000/offline_transaction`)
            .then(
                res => {
                    setTransactions(res.data);
                }
            ).catch(
                err => {
                    alert(err);
                }
            )
    }, [props.isAdmin]);
    const columns = [

        {
            Header: 'ID',
            accessor: 'id',
        },
        {
            Header: 'CARD ID',
            accessor: 'card_id',
        },
        {
            Header: 'Vendor ID',
            accessor: 'vm_id',
        },
        {
            Header: 'Amount',
            accessor: 'remaining_amount',
        },
        {
            Header: 'Timestamp',
            accessor: 'timestamp',
        },
        {
            Header: 'Last Vendor ID',
            accessor: 'prev_vm_id',
        },
        {
            Header: 'Last Amount',
            accessor: 'prev_remaining_amount',
        },
        {
            Header: 'Last trasaction Timestamp',
            accessor: 'prev_timestamp',
        },
        {
            Header: 'Complete trasaction',
            accessor: 'complete',
        },
        {
            Header: 'Transaction sequence',
            accessor: 'transaction_sequence',
        }
    ];
    return (
        <div className="Home">

            <div className="lander">

                <Link to="/changePassword">
                    <Button>
                        Change Password
                    </Button>
                </Link>
                {
                    props.isAdmin &&
                    (
                        <Link to="/register">
                            <Button>
                                Add user
                            </Button>
                        </Link>
                    )
                }
            </div>
            {
                props.isAdmin &&
                (
                <div>
                  <form onSubmit={handleSubmit}>
                    <div>
                        <h3>Vendor Transactions</h3>
                        <NumberedList isAdmin={props.isAdmin} vendors={vendors}/>
                    </div>
                    <div>
                        <h3>All Transactions: </h3>
                        < Styles>
                            < ReactTable columns={columns} data={transactions}/>
                        </Styles>
                    </div>
                    <h1>Enter the details below</h1>
                    <FormGroup md="4" controlId="amount" bssize="small">
                        <FormLabel>Amount wanted</FormLabel>
                        <FormControl

                            type="Amount wanted"
                            value={amount}
                            onChange={e => setAmount(e.target.value)}

                        />
                    </FormGroup>
                    <FormGroup md="4" controlId="vendID" bssize="small">
                        <FormLabel>Vendor ID</FormLabel>
                        <FormControl

                            type="Vendor ID"
                            value={vendID}
                            onChange={e => setVendID(e.target.value)}

                        />
                    </FormGroup>

                        <Button block bssize="large" disabled={!validateForm()} type="submit">
                            Done
                        </Button>

                      </form>

                </div>)

            }
        </div>
    );
}
