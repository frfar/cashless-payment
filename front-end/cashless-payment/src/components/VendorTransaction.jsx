import React, {useEffect, useState} from "react";
import "./Home.css";
import ReactTable from "./ReactTable";
import axios from "axios";
import styled from "styled-components";
import {Redirect} from "react-router-dom";


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


export default function VendorTransaction(props) {
    const [vendorTransact, setVendorTransact] = useState([]);
    useEffect(() => {
        props.location.vendor !== undefined && axios.get(`http://localhost:3000/vendor_transaction?vendor_id=${props.location.vendor.id}`)
            .then(
                result => {
                    setVendorTransact(result.data);
                }
            )
    }, []);
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
            accessor: 'vendor_id',
        },
        {
            Header: 'Amount',
            accessor: 'collected_amount',
        },
        {
            Header: 'Timestamp',
            accessor: 'timestamp',
        }
    ];
    const vendor = props.location.vendor;
    return vendor !== undefined ? (
        <div className="VendorTransaction">
            <h3>Transactions of {vendor.name}</h3>
            <Styles>
            <ReactTable columns={columns} data={vendorTransact}/>
            </Styles>
        </div>
    ) : (<Redirect to="/" />);
}