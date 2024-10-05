# Cinema Ticket

This project is a ticket purchasing service that validates and processes ticket purchase requests.
It ensures that the requests meet certain criteria before proceeding with seat reservations and payment processing.

## Installation

To install and run this project, you need to have Java and Maven installed on your machine.

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/ticket-service.git
    cd cinema-ticket
    ```

2. Build the project using Maven:
    ```sh
    mvn clean install
    ```

## Usage

To use the `TicketServiceImpl` class, you need to create instances of `SeatReservationService` and `TicketPaymentService`,
and then call the `purchaseTickets` method with the appropriate parameters.
