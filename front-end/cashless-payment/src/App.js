import React from 'react'
import axios from "axios";
import {Link} from "react-router-dom";
import {Navbar, Nav, Button} from "react-bootstrap";
import "./App.css";
import 'bootstrap/dist/css/bootstrap.min.css';
import Routes from "./Routes";
import Container from "react-bootstrap/Container";


class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            transactions: [],
            isLoggedIn: false,
            isAdmin: false,
            userToken: '',
            userInfo: {}
        }
        this.handleLogin = this.handleLogin.bind(this);
        this.handleLogout = this.handleLogout.bind(this);
    }

    componentDidMount() {
        axios.get(`http://localhost:3000/offline_transaction`)
            .then(
                res => {
                    const transactions = res.data;
                    this.setState(
                        {
                            transactions: transactions
                        }
                    )
                }
            ).catch(
            err => {
                alert(err);
            }
        )
    }


    handleLogin(userInfo, userToken){

        this.setState({
            userToken: userToken,
            userInfo: userInfo,
            isAdmin: userInfo.isAdmin,
            isLoggedIn: true
        })
    }

    handleLogout() {
        this.setState({
            userToken: '',
            isLoggedIn: false,
            isAdmin: false
        })
    }

    loginButton(isLoggedIn, handleLogout){
        if (isLoggedIn){
            return (
                <Nav.Item onClick={handleLogout}>
                    <Button variant="link">Logout</Button>
                </Nav.Item>
            )
        } else {
            return (
                <Nav.Item>
                    <Link to="/login">
                        Login
                    </Link>
                </Nav.Item>
            )
        }
    }

    render() {

        const data = this.state.transactions;
        const isLoggedIn = this.state.isLoggedIn;

        return (
            <Container className="App">
                <Navbar fluid="true" bg="light">

                    <Navbar.Brand>
                        <Link to="/">Cashless-Payment</Link>
                    </Navbar.Brand>
                    <Nav className="mr-auto">
                    </Nav>

                    <Nav>
                        {this.loginButton(isLoggedIn, this.handleLogout)}
                    </Nav>
                    <Navbar.Toggle/>

                </Navbar>
                <Routes
                    isLoggedIn={isLoggedIn}
                    data={data}
                    isAdmin={this.state.isAdmin}
                    userToken = {this.state.userToken}
                    handleLogin={(userInfo, userToken) => this.handleLogin(userInfo, userToken)}
                />
            </Container>

        )
    }
}

export default App
