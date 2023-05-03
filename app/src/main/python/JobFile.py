from abc import ABC, abstractmethod


class Job(ABC):

    @abstractmethod
    def do_job(self):
        pass

    @abstractmethod
    def scan(self):
        pass
