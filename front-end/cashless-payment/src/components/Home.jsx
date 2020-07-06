import React from "react";
import "./Home.css";
import styled from 'styled-components'
import {useTable} from 'react-table'
import {Link} from "react-router-dom";
import {Button, Table} from "react-bootstrap";

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

function ReactTable({columns, data}) {
    // Use the state and functions returned from useTable to build your UI
    const {
        getTableProps,
        getTableBodyProps,
        headerGroups,
        rows,
        prepareRow,
    } = useTable({
        columns,
        data,
    })

    // Render the UI for your table
    return (
        <Table responsive {...getTableProps()}>
            <thead>
            {headerGroups.map(headerGroup => (
                <tr {...headerGroup.getHeaderGroupProps()}>
                    {headerGroup.headers.map(column => (
                        <th {...column.getHeaderProps()}>{column.render('Header')}</th>
                    ))}
                </tr>
            ))}
            </thead>
            <tbody {...getTableBodyProps()}>
            {rows.map((row, i) => {
                prepareRow(row)
                return (
                    <tr {...row.getRowProps()}>
                        {row.cells.map(cell => {
                            return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>
                        })}
                    </tr>
                )
            })}
            </tbody>
        </Table>
    )
}

export default function Home(props) {
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
    const data = props.isAdmin ? props.data : [];
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
                (<div>
                    <h3>Transactions: </h3>
                    < Styles>
                        < ReactTable columns={columns} data={data}/>
                    </Styles>
                </div>)
            }
        </div>
    );
}