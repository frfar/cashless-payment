import React, {useEffect, useState} from "react";
import "./Home.css";
import {Link} from "react-router-dom";
import {Button} from "react-bootstrap";
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
                </div>)
            }
        </div>
    );
}