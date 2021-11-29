# tec502-pbl2-fog

<p align="center">
  <img src="https://i.imgur.com/3o0SHg4.png" alt="fog icon" width="300px" height="300px">
</p>

------------

## 📚 Descrição ##
**Resolução do problema 2 do MI - Concorrência e Conectividade (TEC 502) - [Universidade Estadual de Feira de Santana (UEFS)](https://www.uefs.br/).**<br/><br/>
O projeto trata-se de uma *Fog*, que é responsável por lidar diretamente com os emuladores de sensores. Essa comunicação é realizada através do protoclo *MQTT*, em que, cada *thread* da *Fog* lida com uma quantidade fixa de emuladores, sempre criando, de forma dinâmica, novas *threads* caso uma outra já esteja "cheia". Além disso, esta *Fog* se comunica diretamente com o servidor principal através de *sockets*, e quando solicitada, envia para o mesmo, uma lista contendo os dados se todos os emuladores que estão conectadas com ela.

### ⛵ Navegação pelos projetos: ###
- [Servidor](https://github.com/AllanCapistrano/tec502-pbl2-server)
- \> Fog
- [Emulador de Sensores](https://github.com/JoaoErick/tec502-pbl2-emulator)
- [Monitoramento de Pacientes](https://github.com/JoaoErick/tec502-pbl2-monitoring)

### 🔗 Tecnologias utilizadas: ### 
- [Java JDK 8](https://www.oracle.com/br/java/technologies/javase/javase-jdk8-downloads.html)

### 📊 Dependências: ### 
- [JSON](https://www.json.org/json-en.html)
- [Eclipse Paho](https://www.eclipse.org/paho/index.php?page=clients/java/index.php)

------------

## 🖥️ Como utilizar ##
Para o utilizar este projeto é necessário ter instalado o JDK 8u111.

- [JDK 8u111 com Netbeans 8.2](https://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-3413139-esa.html)
- [JDK 8u111](https://www.oracle.com/br/java/technologies/javase/javase8-archive-downloads.html)

### Através de uma IDE ###
1. Primeiramente verifique se a biblioteca `org.eclipse.paho.client.mqttv3-1.2.5.jar` já está adicionada ao projeto;
2. Caso não esteja, não é necessário fazer o *download*, a mesma está disponível em `src` > `libs` > [`org.eclipse.paho.client.mqttv3-1.2.5.jar`](https://github.com/AllanCapistrano/tec502-pbl2-fog/blob/main/src/libs/org.eclipse.paho.client.mqttv3-1.2.5.jar), sendo necessário somente adicioná-la de acordo com a sua IDE;
3. Após isso, basta **executar o projeto**, por exemplo, utilizando o *NetBeans IDE 8.2* aperte o botão `F6`;
4. Selecione a região da *Fog*.

### Através do [Docker](https://www.docker.com/) ###
1. Caso ainda não tenha instalado, é necessário instalar o [Docker](https://www.docker.com/get-started);
2. Após instalar o [Docker](https://www.docker.com/get-started), clone este projeto:
- SSH:
```powershell
$ git clone git@github.com:AllanCapistrano/tec502-pbl2-fog.git
```
- HTTPS:
```powershell
$ git clone https://github.com/AllanCapistrano/tec502-pbl2-fog.git
```
3. Com o terminal aberto no diretório do projeto, entre na *branch* que possui o arquivo `dockerfile`;
```powershell
$ git checkout docker
```
3. Faça o *build* da imagem:
```powershell
$ docker build -t tec502_pbl2_fog .
```
4. Inicie o container que irá rodar o projeto:
```powershell
$ docker run -i tec502_pbl2_fog
```
5. Selecione a região da *Fog*.

------------

## 📌 Autores ##
- Allan Capistrano: [Github](https://github.com/AllanCapistrano) - [Linkedin](https://www.linkedin.com/in/allancapistrano/) - [E-mail](https://mail.google.com/mail/u/0/?view=cm&fs=1&tf=1&source=mailto&to=asantos@ecomp.uefs.br)
- João Erick Barbosa: [Github](https://github.com/JoaoErick) - [Linkedin](https://www.linkedin.com/in/joão-erick-barbosa-9050801b0/) - [E-mail](https://mail.google.com/mail/u/0/?view=cm&fs=1&tf=1&source=mailto&to=jsilva@ecomp.uefs.br)

------------

## ⚖️ Licença ##
[MIT License (MIT)](./LICENSE)

