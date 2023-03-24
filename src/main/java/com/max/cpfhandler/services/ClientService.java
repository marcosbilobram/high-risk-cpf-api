package com.max.cpfhandler.services;

import com.max.cpfhandler.dto.ClientDTO;
import com.max.cpfhandler.dto.CpfDTO;
import com.max.cpfhandler.entities.CPF;
import com.max.cpfhandler.entities.Client;
import com.max.cpfhandler.exceptions.ExistsCpfException;
import com.max.cpfhandler.exceptions.InvalidCpfException;
import com.max.cpfhandler.exceptions.NotFoundCpfException;
import com.max.cpfhandler.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    ClientRepository repo;

    @Autowired
    CpfService cpfService;

    public List<Client> findAll(){
        return repo.findAll();
    }

    public Client findById(Long id) {
        return repo.findById(id).get();
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Client insert(Client client) {
        if(!cpfService.validateCPF(client.getCpf().getCpf()))
            throw new InvalidCpfException("CPF is not valid.");

        client.getCpf().setCpf(cpfService.removeNotNumberCharacters(client.getCpf().getCpf()));
        client.getCpf().setCreatedAt(Calendar.getInstance());

        return repo.save(client);
    }

    public Client update(Client client) {
        Client inDBclient = findById(client.getId());
        dataUpdate(inDBclient, client);
        return repo.save(inDBclient);
    }

    public void cpfMayBeAFraud(String cpf){
        Client client = findClientByCpfWithValidation(cpf);
        if(client.getCpf().isItaFraud()){
            throw new ExistsCpfException("CPF" + cpf + "is alerady on the fraud list");
        }

        client.getCpf().setCanBeaFraud(true);
        repo.save(client);
    }

    public void cpfIsNotAFraud(String cpf){
        Client client = findClientByCpfWithValidation(cpf);
        client.getCpf().setCanBeaFraud(false);

        repo.save(client);
    }

    public Client findClientByCpfWithValidation(String cpf){
        cpfService.validateCPF(cpf);
        String treatedCPF;

        treatedCPF = cpfService.removeNotNumberCharacters(cpf);
        Client client = repo.findClientCpfByCpfValue(treatedCPF);

        return client;
    }

    public CPF checkIfCpfIsSavedAsFraud(String cpf){
        Client client = findClientByCpfWithValidation(cpf);
        if (client.getCpf().isItaFraud()){
            return client.getCpf();
        }
        else {
            throw new NotFoundCpfException("The CPF: " + cpf + " is not on the fraud list");
        }
    }

    public void dataUpdate(Client inDBclient, Client client) {
        inDBclient.setName(client.getName());
        inDBclient.setCpf(client.getCpf());
    }

    public Client fromClientDTO(ClientDTO clientDTO){
        return new Client(clientDTO.getId(), clientDTO.getName(), clientDTO.getCpf());
    }

    public CPF fromCPFDTO(CpfDTO cpf) {
        return new CPF(
                cpf.getCpf(),
                cpf.getCreatedAt());
    }



}