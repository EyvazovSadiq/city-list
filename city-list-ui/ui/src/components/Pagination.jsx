import 'bootstrap/dist/css/bootstrap.min.css';
import {PaginationControl} from "react-bootstrap-pagination-control";

export const Pagination = ({currentPage = 1, totalElements, onPageChange}) => {
    return <PaginationControl
        page={currentPage}
        total={totalElements}
        limit={12}
        between={4}
        changePage={(page) => {
            onPageChange(page);
        }}
        ellipsis={1}
    />
}