# Concordium Pay&Verify Demo

It's a demo online store requiring age verification to pay for its goods.

## Environment variables

| Name                    | Meaning                                                                                                                          |                                                                          
|:------------------------|:---------------------------------------------------------------------------------------------------------------------------------|
| `PUBLIC_URL`            | HTTP(S) root URL of the demo, used in for payment QR codes                                                                       |
| `STORE_ACCOUNT_ADDRESS` | Address of the account to which to receive payments                                                                              |
| `STORE_TOKEN_INDEX`     | Contract index of the CIS-2 token in which to receive payments. Subindex is always 0 and the token ID is the default one (empty) |
| `WEB3ID_VERIFIER_URL`   | HTTP(S) root URL of the [web3id-verifier](https://github.com/Concordium/concordium-web3id/tree/main/services/web3id-verifier)    |
| `WALLET_PROXY_URL`      | HTTP(S) root URL of the [wallet-proxy](https://github.com/Concordium/concordium-wallet-proxy)                                    |
| `CCD_EXPLORER_URL`      | Either `https://ccdexplorer.io/testnet` or `https://ccdexplorer.io/mainnet`                                                      |
