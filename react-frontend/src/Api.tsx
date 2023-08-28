import { MembershipList } from "./Type";

export const fetchMembers = async (): Promise<MembershipList> => {
    return fetch('http://localhost:8040/api/course/users')
        .then(res => res.json());
}