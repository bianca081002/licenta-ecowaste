from dotenv import load_dotenv()
if __name__ == "__main__":
    load_dotenv()
    session = get_mongo_session()
    user_repository = UserRepository(seesion)
    print(user_repository.get_users())
